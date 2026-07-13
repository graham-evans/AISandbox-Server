# Nagle's Algorithm and setting TCP No-Delay

Most simulations use some sort of request/response exchange of small messages: the server sends 
the state, the client sends back an action, and the simulation cannot advance until the action
arrives. However, most operating systems and programming languages are optimised for large messages,
and try to reduce network traffic by joining small messages together. In tests we've seen two
standard TCP optimisations interacting badly with simulation traffic, and together they can cost
between 40-200ms per message.

- [Nagle's Algorithm](https://en.wikipedia.org/wiki/Nagle%27s_algorithm) (on by default in most
languages) improves efficiency for bulk transfers by refusing to put a second small segment on the
wire while an earlier small segment is still unacknowledged. It batches small writes rather than
sending a packet per small message.
- Delayed ACK (on by default in most OSs') is the receiver-side counterpart: rather than acknowledging
immediately, the receiver waits hoping to piggyback the ACK onto its own reply, saving a packet.

Individually both are sensible. Together, in a request/response protocol, they can deadlock against
each other. A message written as two pieces — a length prefix followed by the body, (which is what 
protobuf framing does) sends the prefix immediately, then Nagle holds the body until the prefix is
acknowledged. But the receiver has nothing to reply with yet (it needs the body before it can act),
so it has nothing to piggyback on, and its delayed-ACK timer runs the full ~40ms (200ms in Windows). 
The ACK finally fires, the body is released instantly, and the exchange completes. The same stall
then happens on the return leg. Neither side did any work during those 80ms; both were waiting on
the other's timer.

## TCP No Delay

Setting TCP_NODELAY within each both the client and server, disables Nagle's algorithm when sending
messages, so small writes go out immediately instead of waiting for an ACK that isn't coming. As the
receiving application sees all the message stream, it immediately acknowledges the message and
continue the simulation.

This code has been added to the server as of version 2.1, but needs to be added to your client code
as well.

### Java

```java
Socket clientSocket = new Socket(host, port);
clientSocket.setTcpNoDelay(true);
```

### Python

```python
connection = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
connection.connect((host, port))
connection.setsockopt(socket.IPPROTO_TCP, socket.TCP_NODELAY, 1)
```

### Go

Go already has this switched off by default, but can be explicitly set:

```Go
conn, _ := net.Dial("tcp", "localhost:9000")
conn.(*net.TCPConn).SetNoDelay(true)  // already the default
```

### C / C++

```C
#include <netinet/tcp.h>
int flag = 1;
setsockopt(fd, IPPROTO_TCP, TCP_NODELAY, &flag, sizeof(flag));
```

(On Windows, #include <winsock2.h> and cast the flag to const char*.)

### C#

```csharp
var client = new TcpClient();
client.Connect("localhost", 9000);
client.NoDelay = true;
// or on a raw Socket:  socket.NoDelay = true;
```

### JavaScript / TypeScript (Node.js)

```javascript
const socket = net.createConnection({ host: 'localhost', port: 9000 });
socket.setNoDelay(true);
```

### Rust

```rust
let stream = TcpStream::connect("127.0.0.1:9000")?;
stream.set_nodelay(true)?;
```

### Kotlin

```kotlin
val socket = Socket("localhost", 9000)
socket.tcpNoDelay = true
```

### Ruby

```ruby
require 'socket'
sock = TCPSocket.new('localhost', 9000)
sock.setsockopt(Socket::IPPROTO_TCP, Socket::TCP_NODELAY, 1)
```

### PHP

PHP needs the ```sockets``` extension as the stream API doesn't expose this cleanly.

```php
$sock = socket_create(AF_INET, SOCK_STREAM, SOL_TCP);
socket_connect($sock, '127.0.0.1', 9000);
socket_set_option($sock, SOL_TCP, TCP_NODELAY, 1);
```

### Swift

This example used the ```Network``` framework

```swift
let opts = NWProtocolTCP.Options()
opts.noDelay = true
let params = NWParameters(tls: nil, tcp: opts)
let conn = NWConnection(host: "localhost", port: 9000, using: params)
```

### Dart

```dart
final socket = await Socket.connect('localhost', 9000);
socket.setOption(SocketOption.tcpNoDelay, true);
```