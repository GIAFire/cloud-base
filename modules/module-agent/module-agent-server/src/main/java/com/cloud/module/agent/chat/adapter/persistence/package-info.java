/**
 * MyBatis 聊天存储 Adapter。
 *
 * <p>MybatisChatStore 负责 Reactor、阻塞 JDBC 和租户 ThreadLocal 之间的桥接；
 * ChatStoreTransactions 负责真正的同步事务和行锁顺序。</p>
 */
package com.cloud.module.agent.chat.adapter.persistence;
