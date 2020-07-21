package com.jincou.authserver.oauth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;


/**
 * @Description: 授权服务器配置
 *   使用 @EnableAuthorizationServer 来配置授权服务机制，并继承 AuthorizationServerConfigurerAdapter 该类重写 configure 方法定义授权服务器策略
 *
 * @author xub
 * @date 2020/7/21 下午5:12
 */
@Configuration
@EnableAuthorizationServer
public class OAuth2AuthorizationServer extends AuthorizationServerConfigurerAdapter {

    @Autowired
    private AuthenticationManager authenticationManager;

    /**
     * 使用同一个密钥来编码 JWT 中的  OAuth2 令牌
     */
    @Bean
    public JwtAccessTokenConverter accessTokenConverter() {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        converter.setSigningKey("dada");
        return converter;
    }

    @Bean
    public JwtTokenStore jwtTokenStore() {
        return new JwtTokenStore(accessTokenConverter());
    }

    /**
     * 告诉Spring Security Token的生成方式
     */
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints
            .authenticationManager(authenticationManager)
            .tokenStore(jwtTokenStore())
            .accessTokenConverter(accessTokenConverter());
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        //添加客户端信息 使用in-memory存储客户端信息
        clients.inMemory()
                //客户端标识 ID
            .withClient("clientapp")
                //客户密钥
            .secret("112233")
                //客户端访问范围，默认为空则拥有全部范围
            .scopes("read_userinfo")
                //客户端使用的授权类型，默认为空
            .authorizedGrantTypes(
                "password",
                "authorization_code",
                "refresh_token");
    }
}
