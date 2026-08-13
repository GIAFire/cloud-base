<template>
  <view class="yd-page-container yd-page-container-paging">
    <!-- 顶部导航栏 -->
    <wd-navbar
      title="会员用户"
      left-arrow placeholder safe-area-inset-top fixed
      @click-left="handleBack"
    />

    <!-- 搜索组件 -->
    <SearchForm @search="handleQuery" @reset="handleReset" />

    <!-- 会员列表 -->
    <z-paging
      ref="pagingRef"
      v-model="list"
      :fixed="false"
      class="min-h-0 flex-1"
      :default-page-size="10"
      :refresher-enabled="true"
      :inside-more="true"
      :loading-more-default-as-loading="true"
      empty-view-text="暂无会员用户数据"
      @query="queryList"
    >
      <view class="p-24rpx">
        <view
          v-for="item in list"
          :key="item.id"
          class="mb-24rpx overflow-hidden rounded-12rpx bg-white shadow-sm"
          @click="handleDetail(item)"
        >
          <view class="p-24rpx">
            <view class="mb-20rpx flex items-start justify-between gap-16rpx">
              <view class="min-w-0 flex flex-1 items-center gap-16rpx">
                <wd-img
                  v-if="item.avatar"
                  :src="item.avatar"
                  :width="44"
                  :height="44"
                  mode="aspectFill"
                  round
                />
                <view
                  v-else
                  class="h-88rpx w-88rpx flex shrink-0 items-center justify-center rounded-full bg-[#1890ff] text-34rpx text-white"
                >
                  {{ (item.nickname || item.mobile || '会').charAt(0) }}
                </view>
                <view class="min-w-0 flex-1">
                  <view class="truncate text-32rpx text-[#333] font-semibold">
                    {{ item.nickname || item.name || item.mobile || '-' }}
                  </view>
                  <view class="mt-4rpx text-24rpx text-[#999]">
                    {{ item.mobile || '未绑定手机号' }}
                  </view>
                </view>
              </view>
              <dict-tag :type="DICT_TYPE.COMMON_STATUS" :value="item.status" />
            </view>
            <view class="grid grid-cols-3 mb-16rpx gap-12rpx rounded-8rpx bg-[#f7f8fa] p-16rpx text-center">
              <view>
                <view class="text-30rpx text-[#333] font-semibold">
                  {{ item.point ?? 0 }}
                </view>
                <view class="mt-4rpx text-22rpx text-[#999]">
                  当前积分
                </view>
              </view>
              <view>
                <view class="text-30rpx text-[#333] font-semibold">
                  {{ item.experience ?? 0 }}
                </view>
                <view class="mt-4rpx text-22rpx text-[#999]">
                  成长值
                </view>
              </view>
              <view>
                <view class="truncate text-30rpx text-[#333] font-semibold">
                  {{ item.levelName || '-' }}
                </view>
                <view class="mt-4rpx text-22rpx text-[#999]">
                  等级
                </view>
              </view>
            </view>
            <view v-if="item.groupName" class="mb-12rpx flex items-center text-28rpx text-[#666]">
              <text class="mr-8rpx shrink-0 text-[#999]">分组：</text>
              <text>{{ item.groupName }}</text>
            </view>
            <view v-if="item.tagNames?.length" class="mb-12rpx flex flex-wrap items-center gap-8rpx">
              <wd-tag v-for="tag in item.tagNames" :key="tag" type="primary" variant="plain">
                {{ tag }}
              </wd-tag>
            </view>
            <view class="flex items-center justify-between text-24rpx text-[#999]">
              <text>注册：{{ formatDateTime(item.createTime) || '-' }}</text>
              <text>登录：{{ formatDateTime(item.loginDate) || '-' }}</text>
            </view>
          </view>
        </view>
      </view>
    </z-paging>

  </view>
</template>

<script lang="ts" setup>
import type { MemberUser } from '@/api/member/user'
import { onUnload } from '@dcloudio/uni-app'
import { onMounted, ref } from 'vue'
import { getMemberUserPage } from '@/api/member/user'
import { navigateBackPlus } from '@/utils'
import { DICT_TYPE } from '@/utils/constants'
import { formatDateTime } from '@/utils/date'
import SearchForm from './components/search-form.vue'

definePage({
  style: {
    navigationBarTitleText: '',
    navigationStyle: 'custom',
  },
})

const list = ref<MemberUser[]>([]) // 列表数据
const pagingRef = ref<any>() // 分页组件引用
const queryParams = ref<Record<string, any>>({}) // 查询参数

/** 返回上一页 */
function handleBack() {
  navigateBackPlus()
}

/** 查询会员列表 */
async function queryList(pageNo: number, pageSize: number) {
  try {
    const data = await getMemberUserPage({
      ...queryParams.value,
      pageNo,
      pageSize,
    })
    pagingRef.value?.completeByTotal(data.list, data.total)
  } catch {
    pagingRef.value?.complete(false)
  }
}

/** 搜索按钮操作 */
function handleQuery(data?: Record<string, any>) {
  queryParams.value = { ...data }
  reload()
}

/** 重置按钮操作 */
function handleReset() {
  handleQuery()
}

/** 重新加载 */
function reload() {
  pagingRef.value?.reload()
}

/** 查看详情 */
function handleDetail(item: MemberUser) {
  uni.navigateTo({
    url: `/pages-member/user/detail/index?id=${item.id}`,
  })
}

/** 初始化 */
onMounted(() => {
  uni.$on('member:user:reload', reload)
})

/** 卸载 */
onUnload(() => {
  uni.$off('member:user:reload', reload)
})
</script>
