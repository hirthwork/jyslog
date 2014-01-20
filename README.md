jyslog — Syslog client for java
===============================

Introduction
------------

Syslog became a industry standard for quick and painless logging. While people continuing to reinvent logging facitilities such as jul, commons-logging or log4j, syslog stays lightweight and handy logging protocol.

Rationale
---------

The main goal of jyslog is simplicity. That's why user can start logging by creating just single [`Syslogger`](src/main/java/com/reinventedcode/jyslog/Syslogger.java) object which will guide you to the wonderful world of syslogging:

```java
Syslogger syslogger = new BasicSyslogger(new UdpSyslog());
syslogger.alert("Look ma, a syslog message!");
syslogger.info("It is rfc 5424 one!");
```

Features
--------

jyslog supports [RFC3164](http://tools.ietf.org/html/rfc3164) (BSD syslog), [RFC5424](http://tools.ietf.org/html/rfc5424) (syslog itself) and [RFC5426](http://tools.ietf.org/html/rfc5426) (syslog over UDP). TCP and TLS transports are not supported.

Prerequisites
-------------

* As the jyslog uses `java.time.*` facility for syslog timestamp, so you will need JDK 8 to compile jyslog. You can download it [here](https://jdk8.java.net/download.html) or get one provided by IBM.
* Gradle is used as build system, so you will need it, too. You can download it [here](http://www.gradle.org/downloads).

Build
-----
```shell
gradle -Dorg.gradle.java.home=/path/to/jdk8 build
```

Quick start
-----------

As was mentioned above, all logging is done by `Syslogger` object. With this class you can specify message severity, either by function shortcut:

```java
syslogger.warning("Don't be late at school!");
```

or specifying it directly:

```java
syslogger.log(Severity.DEBUG, "We're here and still no sign of exception");
```

For your accommodation, syslog will also add exception stack trace to message, if you pass one:

```java
try {
    channel = ServerSocketChannel.open();
} catch (IOException e) {
    syslogger.critical("Can't create channel", e);
}
```

**NOTE**: Be aware, that some syslog server implementations, such as `rsyslog` will replace all `\n` characters with `#012`, as this is permitted by RFC3164. `syslog-ng` seems to be much more tolerant to control characters.

Using `Syslogger.withProcid` you can create new `Syslogger` instance which will be designated logger for some server session, such as HTTP-request handing, all messages with same `procid` can be grouped by syslog server and logged to separate file:

```java
public HttpSession handle(final HttpRequest request) {
    Syslogger syslogger = rootSyslogger.withProcid(generateSessionId());
    syslogger.debug("Request received: " + request.getRequestLine());
    return new HttpSession(request, syslogger);
}
```

`Syslogger` object are immutable, so all `.with*` operations are thread-safe. This operations intended for syslog header fields manipulation and includes:

* `withFacility`
* `withHostname`
* `withAppname`
* `withProcid`
* `withMsgid`
* `withStructuredData`

More information on headers fields can be learned from [RFC5424 §6.2](http://tools.ietf.org/html/rfc5424#section-6.2). Structured data description can be found in [§6.3](http://tools.ietf.org/html/rfc5424#section-6.3).

Understanding jyslog
--------------------
jyslog consist of two layers:

* Transport layer, which converts log message to raw bytes and sends it to syslog server.
* Application layer, which is responsible for log message generation. Among with message text and exception information saving, this includes message source and timestamp collecting into single [`Record`](src/main/java/com/reinventedcode/jyslog/Record.java) object.

Transport layer in turn consist of two components:

* [`Formatter`](src/main/java/com/reinventedcode/jyslog/Formatter.java), which will convert `Record` to `ByteBuffer`
* [`Syslog`](src/main/java/com/reinventedcode/jyslog/Syslog.java), which will send `ByteBuffer` to syslog server

RFC3164 `Syslog` object can be created simply by:
```java
try (Syslog syslog = new UdpSyslog(Rfc3164FormatterSupplier.INSTANCE)) {
    Syslogger syslogger = new BasicSyslogger(sysylog);
    syslogger.notice("Plain old BSD syslog message");
    syslogger = syslogger.withProcid("someid");
    syslogger.warning("BSD syslog doesn't support 'procid', use .withAppname(...) to set message tag");
}
```

Application layer consist of two components:

* [`SourceInfo`](src/main/java/com/reinventedcode/jyslog/SourceInfo.java) which contains rarely mutable information, such as syslog message header contents (except [`priority`](http://tools.ietf.org/html/rfc5424#section-6.2.1) and [`timestamp`](http://tools.ietf.org/html/rfc5424#section-6.2.3))
* [`Record`](src/main/java/com/reinventedcode/jyslog/Record.java) which contains all information from `SourceInfo` plus message text and exception information.

Application which use jyslog should never create `Record` objects as this is a `Syslogger` job, which in fact a handy shortcut for `Record` constructor.

Examples
--------

### Binding to remote syslog server

```java
Syslog syslog = new UdpSyslog(new InetSocketAddress("example.com", 514));
Syslogger syslogger = new BasicSyslogger(syslog);
syslogger.info("Hello there!");
```

### Setting structured data

```java
Syslogger syslogger = new BasicSyslogger(new UdpSyslog()).withStructuredData(
    new BasicSDElement("timeQuality")
        .param("tzKnown", "0")
        .param("isSynced", "0"),
    new BasicSDElement("origin")
        .param("ip", "192.168.0.33"));
syslogger.info("Here the message with some structured data");
```

### Message transmission error handling
[`UdpSyslog`](src/main/java/com/reinventedcode/jyslog/UdpSyslog.java) constructor accepts [`IOExceptionHandler`](src/main/java/com/reinventedcode/jyslog/IOExceptionHandler.java) object. In case of any `IOException` during log message transmission, `IOExceptionHandler.handleException(IOException)` will be called. So, basic exception checking can be done like this:

```java
BasicIOExceptionHandler handler = new BasicIOExceptionHandler();
Syslogger syslogger = new BasicSyslogger(new UdpSyslog(handler));
for (int i = 0; i < 10000; ++i) {
    syslogger.info("Hello, world!");
}
if (handler.exception() != null) {
    System.err.println("Some log messages lost!");
}
```

**NOTE**: Be aware, that there is no guarantee of such exception when sending single message.
