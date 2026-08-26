/**
 * 聊天业务状态存储 seam。
 *
 * <p>ChatStore 用 begin/finish 两个操作隐藏 Session、Run、消息序号、幂等、
 * 并发互斥和事务细节。内存与 MySQL Adapter 必须遵守同一组接口不变量。</p>
 */
package com.cloud.module.agent.chat.store;
