package com.cloud.module.promotion.framework.rpc.config;

import com.cloud.module.infra.api.websocket.WebSocketSenderApi;
import com.cloud.module.member.api.user.MemberUserApi;
import com.cloud.module.product.api.category.ProductCategoryApi;
import com.cloud.module.product.api.sku.ProductSkuApi;
import com.cloud.module.product.api.spu.ProductSpuApi;
import com.cloud.module.system.api.social.SocialClientApi;
import com.cloud.module.system.api.user.AdminUserApi;
import com.cloud.module.trade.api.order.TradeOrderApi;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration(value = "promotionRpcConfiguration", proxyBeanMethods = false)
@EnableFeignClients(clients = {ProductSkuApi.class, ProductSpuApi.class, ProductCategoryApi.class,
        MemberUserApi.class, TradeOrderApi.class, AdminUserApi.class, SocialClientApi.class,
        WebSocketSenderApi.class})
public class RpcConfiguration {
}
