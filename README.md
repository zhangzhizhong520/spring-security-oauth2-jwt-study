#SpringSceurity(6)---JWT详解

在JWT之前我们在做用户认证的时候,基本上会考虑`session` 和 `token`,所以在讲jwt之前，我们先来回顾下这个两个

##一、传统的session认证

####1、原理流程

session 是基于 cookie 实现的，session 存储在服务器端，sessionId 会被存储到客户端的cookie 中,具体流程如下

（图一）

`session 认证流程`

```
1、用户第一次请求服务器的时候，服务器根据用户提交的相关信息，创建对应的 Session
2、请求返回时将此 Session 的唯一标识信息 SessionID 返回给浏览器
3、浏览器接收到服务器返回的 SessionID 信息后，会将此信息存入到 Cookie 中，同时 Cookie 记录此 SessionID 属于哪个域名
4、当用户第二次访问服务器的时候，请求会自动判断此域名下是否存在 Cookie 信息，如果存在自动将 Cookie 信息也发送给服务端，服务端会
   从 Cookie 中获取 SessionID，再根据 SessionID 查找对应的 Session 信息，如果没有找到说明用户没有登录或者登录失效，如果找到 Session
   证明用户已经登录可执行后面操作。
```

   
   
`总结` 根据以上流程可知，SessionID 是连接 Cookie 和 Session 的一道桥梁，大部分系统也是根据此原理来验证用户登录状态。


####2、session 时需要考虑的问题

```
1、将 session 存储在服务器里面，当用户同时在线量比较多时，这些 session 会占据较多的内存，需要在服务端定期的去清理过期的 session。
2、当网站采用集群部署的时候，会遇到多台 web 服务器之间如何做 session 共享的问题。因为 session 是由单个服务器创建的，但是处理用户请求
   的服务器不一定是那个创建 session 的服务器，那么该服务器就无法拿到之前已经放入到 session 中的登录凭证之类的信息了。
3、当多个应用要共享 session 时，除了以上问题，还会遇到跨域问题，因为不同的应用可能部署的主机不一样，需要在各个应用做好 cookie 跨域的处理。
4、sessionId 是存储在 cookie 中的，假如浏览器禁止 cookie 或不支持 cookie 怎么办？ 一般会把 sessionId 跟在 url 参数后面即重写 url，
  所以 session 不一定非得需要靠 cookie 实现
5、移动端对 cookie 的支持不是很好，而 session 需要基于 cookie 实现，所以移动端常用的是 token
6、CSRF: 因为是基于cookie来进行用户识别的, cookie如果被截获，用户就会很容易受到跨站请求伪造的攻击。
```


##二、Token（令牌）

####1、原理流程

基于token的鉴权机制类似于http协议也是无状态的，`它不需要在服务端去保留用户的认证信息或者会话信息`。

（tu2）
这个不是标准时序图，能看懂大致意思就行。

`token认证流程`

1、客户端使用用户名跟密码请求登录
2、服务端收到请求，去认证服务器验证用户名与密码
3、验证成功后，服务端会签发一个 token 并把这个 token 发送给客户端
4、客户端收到 token 以后，会把它存储起来，比如放在 cookie 里或者 localStorage 里
5、客户端每次向服务端请求资源的时候需要带着服务端签发的 token
6、服务端收到请求，然后去验证客户端请求里面带着的 token ，如果验证成功，就向客户端返回请求的数据


####2、token特点

1、如果你认为用数据库来存储 token 会导致查询时间太长，可以选择放在内存当中。比如 redis 很适合你对 token 查询的需求。
2、token 完全由应用管理，所以它可以避开同源策略
3、token 可以避免 CSRF 攻击(因为不需要 cookie 了)
4、移动端对 cookie 的支持不是很好，而 session 需要基于 cookie 实现，所以移动端常用的是 token


####3、Token 和 Session 的区别

Session 是一种记录服务器和客户端会话状态的机制，使服务端有状态化，可以记录会话信息。而 Token 是令牌，访问资源接口（API）时所需要的资源凭证。Token 

使服务端无状态化，不会存储会话信息。


有关Token 和 Session可以看下这篇博客 [彻底理解cookie、session、token](https://www.cnblogs.com/moyand/p/9047978.html)

<br>

##三、JWT

我们在使用token的时候会发现，前端给我们传了token之后，我们还需要拿者这个token去数据库查询用户信息，并返回。这样数据库的操作肯定会影响一定的性能，那jwt就是来解决这个的。

####1、原理流程

（图3）

`JWT 认证流程`

```
1、用户输入用户名/密码登录，服务端认证成功后，会返回给客户端一个 JWT
2、客户端将 jwt 保存到本地（通常使用 localstorage，也可以使用 cookie）
3、当用户希望访问一个受保护的路由或者资源的时候，需要请求头的 Authorization 字段中使用Bearer 模式添加 JWT，其内容看起来是下面这样
   Authorization: Bearer复制代码
4、服务端的保护路由将会检查请求头 Authorization 中的 JWT 信息，如果合法，则允许用户的行为
```

因为 JWT 是自包含的（内部包含了一些会话信息），因此减少了需要查询数据库的需要

因为 JWT 并不使用 Cookie 的，所以你可以使用任何域名提供你的 API 服务而不需要担心跨域资源共享问题（CORS）

因为用户的状态不再存储在服务端的内存中，所以这是一种无状态的认证机制



####2、Token 和 JWT 的区别

`相同`

1、**都是访问资源的令牌**

2、**都可以记录用户的信息**

3、**都是使服务端无状态化**

4、**都是只有验证成功后，客户端才能访问服务端上受保护的资源**

`区别`

**Token** 服务端验证客户端发送过来的 Token 时，还需要查询数据库获取用户信息，然后验证 Token 是否有效。

**JWT** 将 Token 和 Payload 加密后存储于客户端，服务端只需要使用密钥解密进行校验（校验也是 JWT 自己实现的）即可，不需要查询或者减少查询数据库，

因为 JWT 自包含了用户信息和加密的数据。

3、JWT特点

从优点来讲,它最大的优点就是，当服务端拿到JWT之后,我们不需要向token样还需去查询数据库校验信息，因为JWT中就包含用户信息,所以减少一次数据的查询，

但这样做也会带来很明显的问题

 `1、无法满足修改密码场景`
 
 因为上面说过，服务端拿到jwt是不会在去查询数据库的，所以就算你改了密码，服务端还是未知的。那么假设号被到了，修改密码（是用户密码，不是 jwt 的 secret）之后，
 
 盗号者在原 jwt 有效期之内依旧可以继续访问系统，所以仅仅清空 cookie 自然是不够的，这时，需要强制性的修改 secret。

`2、无法满足注销场景`

传统的 session+cookie 方案用户点击注销，服务端清空 session 即可，因为状态保存在服务端。但 jwt 的方案就比较难办了，因为 jwt 是无状态的，服务端通过计算来校验有效性。没有存储起来，所以即使客户端删除了 jwt，但是该 jwt 还是在有效期内，只不过处于一个游离状态。



`3、无法满足token续签场景`

我们知道微信只要你每天使用是不需要重新登录的，因为有token续签，因为传统的 cookie 续签方案一般都是框架自带的，session 有效期 30 分钟，30 分钟内如果有访问，session 有效期被刷新至 30 分钟。但是 jwt 本身的 payload 之中也有一个 exp 过期时间参数，来代表一个 jwt 的时效性，而 jwt 想延期这个 exp 就有点身不由己了，因为 payload 是参与签名的，一旦过期时间被修改，整个 jwt 串就变了，jwt 的特性天然不支持续签！




<br>

##四、JWT的结构


JWT由三部分组成，分别是 `头信息`，`有效载荷`,`签名` 中间以 点(.) 分隔，具体如下

```
  xxxxx.yyyyy.zzzzz
```

####1、header(头信息)


由两部分组成，**令牌类型**（即：JWT）、**散列算法**（HMAC、RSASSA、RSASSA-PSS等），例如：

```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```
然后，这个JSON被编码为Base64Url，形成JWT的第一部分。

####2、Payload（有效载荷）

JWT的第二部分是payload，其中包含claims。claims是关于实体（常用的是用户信息）和其他数据的声明，claims有三种类型： registered, public, and private claims。
**Registered claims**：这些是一组预定义的claims，非强制性的，但是推荐使用， iss（发行人）， exp（到期时间）， sub（主题）， aud（观众）等；
**Public claims**: 自定义claims，注意不要和JWT注册表中属性冲突，这里可以查看JWT注册表
**Private claims**: 这些是自定义的claims，用于在同意使用这些claims的各方之间共享信息，它们既不是Registered claims，也不是Public claims。

以下是payload示例：

```json
 {
   "sub": "1234567890",
   "name": "John Doe",
   "admin": true
 }
```

然后，再经过Base64Url编码，形成JWT的第二部分；

`注意`：对于签名令牌，此信息虽然可以防止篡改，但任何人都可以读取。除非加密，否则不要将敏感信息放入到Payload或Header元素中。

####3、Signature(签名)

jwt的第三部分是一个签证信息，这个签证信息由三部分组成：**header (base64后的)** ，**payload (base64后的)**，**secret**

这个部分需要base64加密后的header和base64加密后的payload使用.连接组成的字符串，然后通过header中声明的加密方式进行加盐secret组合加密，然后就构成了jwt的第三部分。

```javascript
var encodedString = base64UrlEncode(header) + '.' + base64UrlEncode(payload);

var signature = HMACSHA256(encodedString, 'secret'); // TJVA95OrM7E2cBab30RMHrHDcEfxjoYZgeFONFh7HgQ
```
将这三部分用.连接成一个完整的字符串,构成了最终的jwt:

```
 eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWV9.TJVA95OrM7E2cBab30RMHrHDcEfxjoYZgeFONFh7HgQ
```

注意：secret是保存在服务器端的，jwt的签发生成也是在服务器端的，secret就是用来进行jwt的签发和jwt的验证，所以，它就是你服务端的私钥，在任何场景都不应该流露出去。一旦客户端得知这个secret, 那就意味着客户端是可以自我签发jwt了。


####4、如何应用

一般是在请求头里加入Authorization，并加上Bearer标注：

```json
fetch('api/user/1', {
  headers: {
    'Authorization': 'Bearer ' + token
  }
})
```

<br>

###<font color=#FFD700>参考</font>

1、[Cookie、Session、Token、JWT](https://www.cnblogs.com/bigzhan/p/12560567.html) 


<br>
<br>

```
别人骂我胖，我会生气，因为我心里承认了我胖。别人说我矮，我就会觉得好笑，因为我心里知道我不可能矮。这就是我们为什么会对别人的攻击生气。
攻我盾者，乃我内心之矛(24)
```



















# spring-security-oauth2-jwt-study
