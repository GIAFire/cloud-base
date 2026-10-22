package com.cloud.module.trade.framework.delivery.core.client;

import com.cloud.module.trade.framework.delivery.core.client.dto.ExpressTrackQueryReqDTO;
import com.cloud.module.trade.framework.delivery.core.client.dto.ExpressTrackRespDTO;

import java.util.List;

/**
 * 快递客户端接口
 *
 * @author jason
 */
public interface ExpressClient {

    /**
     * 快递实时查询
     *
     * @param reqDTO 查询请求参数
     */
    List<ExpressTrackRespDTO> getExpressTrackList(ExpressTrackQueryReqDTO reqDTO);

}
