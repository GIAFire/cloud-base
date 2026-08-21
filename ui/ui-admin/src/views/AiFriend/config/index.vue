<template>
  <ContentWrap>
    <!-- 搜索工作栏 -->
    <el-form
      class="-mb-15px"
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
      label-width="68px"
    >
      <el-form-item label="Agent 主键ID" prop="id">
        <el-input
          v-model="queryParams.id"
          placeholder="请输入Agent 主键ID"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="关联 ai_agent_model.id" prop="modelId">
        <el-input
          v-model="queryParams.modelId"
          placeholder="请输入关联 ai_agent_model.id"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="关联 ai_agent_sys_prompt.id" prop="sysPromptId">
        <el-input
          v-model="queryParams.sysPromptId"
          placeholder="请输入关联 ai_agent_sys_prompt.id"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="Agent唯一编码" prop="agentCode">
        <el-input
          v-model="queryParams.agentCode"
          placeholder="请输入Agent唯一编码"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="Agent 显示名称" prop="agentName">
        <el-input
          v-model="queryParams.agentName"
          placeholder="请输入Agent 显示名称"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="最大循环次数" prop="maxIters">
        <el-input
          v-model="queryParams.maxIters"
          placeholder="请输入最大循环次数"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="默认权限模式" prop="permissionMode">
        <el-select
          v-model="queryParams.permissionMode"
          placeholder="请选择默认权限模式"
          clearable
          class="!w-240px"
        >
          <el-option
            v-for="dict in getStrDictOptions(DICT_TYPE.AGENT_PERMISSION_TYPE)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="是否启用上下文压缩：1启用，0关闭" prop="compactionEnabled">
        <el-select
          v-model="queryParams.compactionEnabled"
          placeholder="请选择是否启用上下文压缩：1启用，0关闭"
          clearable
          class="!w-240px"
        >
          <el-option
            v-for="dict in getBoolDictOptions(DICT_TYPE.INFRA_BOOLEAN_STRING)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="触发压缩消息数" prop="triggerMessages">
        <el-input
          v-model="queryParams.triggerMessages"
          placeholder="请输入触发压缩消息数"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="压缩后保留消息数" prop="keepMessages">
        <el-input
          v-model="queryParams.keepMessages"
          placeholder="请输入压缩后保留消息数"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="触发压缩token数" prop="triggerTokens">
        <el-input
          v-model="queryParams.triggerTokens"
          placeholder="请输入触发压缩token数"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="压缩后保留token数" prop="keepTokens">
        <el-input
          v-model="queryParams.keepTokens"
          placeholder="请输入压缩后保留token数"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="是否启用大工具结果卸载：1启用，0关闭" prop="toolResultEvictionEnabled">
        <el-select
          v-model="queryParams.toolResultEvictionEnabled"
          placeholder="请选择是否启用大工具结果卸载：1启用，0关闭"
          clearable
          class="!w-240px"
        >
          <el-option
            v-for="dict in getBoolDictOptions(DICT_TYPE.INFRA_BOOLEAN_STRING)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="是否启用长期记忆:1启用,0禁用" prop="memoryEnable">
        <el-select
          v-model="queryParams.memoryEnable"
          placeholder="请选择是否启用长期记忆:1启用,0禁用"
          clearable
          class="!w-240px"
        >
          <el-option
            v-for="dict in getBoolDictOptions(DICT_TYPE.INFRA_BOOLEAN_STRING)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="是否启用Plan Mode：1启用，0关闭" prop="planModeEnabled">
        <el-select
          v-model="queryParams.planModeEnabled"
          placeholder="请选择是否启用Plan Mode：1启用，0关闭"
          clearable
          class="!w-240px"
        >
          <el-option
            v-for="dict in getBoolDictOptions(DICT_TYPE.INFRA_BOOLEAN_STRING)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="Plan阶段是否允许shell工具：1允许，0禁止；生产环境建议关闭" prop="allowShellInPlanMode">
        <el-select
          v-model="queryParams.allowShellInPlanMode"
          placeholder="请选择Plan阶段是否允许shell工具：1允许，0禁止；生产环境建议关闭"
          clearable
          class="!w-240px"
        >
          <el-option
            v-for="dict in getBoolDictOptions(DICT_TYPE.INFRA_BOOLEAN_STRING)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="创建时间" prop="createTime">
        <el-date-picker
          v-model="queryParams.createTime"
          value-format="YYYY-MM-DD HH:mm:ss"
          type="daterange"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]"
          class="!w-220px"
        />
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button
          type="primary"
          plain
          @click="openForm('create')"
          v-hasPermi="['AiFriend:agent-info:create']"
        >
          <Icon icon="ep:plus" class="mr-5px" /> 新增
        </el-button>
        <el-button
          type="success"
          plain
          @click="handleExport"
          :loading="exportLoading"
          v-hasPermi="['AiFriend:agent-info:export']"
        >
          <Icon icon="ep:download" class="mr-5px" /> 导出
        </el-button>
        <el-button
            type="danger"
            plain
            :disabled="isEmpty(checkedIds)"
            @click="handleDeleteBatch"
            v-hasPermi="['AiFriend:agent-info:delete']"
        >
          <Icon icon="ep:delete" class="mr-5px" /> 批量删除
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table
        row-key="id"
        v-loading="loading"
        :data="list"
        :stripe="true"
        :show-overflow-tooltip="true"
        @selection-change="handleRowCheckboxChange"
    >
    <el-table-column type="selection" width="55" />
      <el-table-column label="Agent 主键ID" align="center" prop="id" />
      <el-table-column label="关联 ai_agent_model.id" align="center" prop="modelId" />
      <el-table-column label="关联 ai_agent_sys_prompt.id" align="center" prop="sysPromptId" />
      <el-table-column label="Agent唯一编码" align="center" prop="agentCode" />
      <el-table-column label="Agent 显示名称" align="center" prop="agentName" />
      <el-table-column label="Agent 描述" align="center" prop="description" />
      <el-table-column label="最大循环次数" align="center" prop="maxIters" />
      <el-table-column label="默认权限模式" align="center" prop="permissionMode">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.AGENT_PERMISSION_TYPE" :value="scope.row.permissionMode" />
        </template>
      </el-table-column>
      <el-table-column label="是否启用上下文压缩：1启用，0关闭" align="center" prop="compactionEnabled">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.INFRA_BOOLEAN_STRING" :value="scope.row.compactionEnabled" />
        </template>
      </el-table-column>
      <el-table-column label="触发压缩消息数" align="center" prop="triggerMessages" />
      <el-table-column label="压缩后保留消息数" align="center" prop="keepMessages" />
      <el-table-column label="触发压缩token数" align="center" prop="triggerTokens" />
      <el-table-column label="压缩后保留token数" align="center" prop="keepTokens" />
      <el-table-column label="是否启用大工具结果卸载：1启用，0关闭" align="center" prop="toolResultEvictionEnabled">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.INFRA_BOOLEAN_STRING" :value="scope.row.toolResultEvictionEnabled" />
        </template>
      </el-table-column>
      <el-table-column label="是否启用长期记忆:1启用,0禁用" align="center" prop="memoryEnable">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.INFRA_BOOLEAN_STRING" :value="scope.row.memoryEnable" />
        </template>
      </el-table-column>
      <el-table-column label="是否启用Plan Mode：1启用，0关闭" align="center" prop="planModeEnabled">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.INFRA_BOOLEAN_STRING" :value="scope.row.planModeEnabled" />
        </template>
      </el-table-column>
      <el-table-column label="Plan阶段是否允许shell工具：1允许，0禁止；生产环境建议关闭" align="center" prop="allowShellInPlanMode">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.INFRA_BOOLEAN_STRING" :value="scope.row.allowShellInPlanMode" />
        </template>
      </el-table-column>
      <el-table-column
        label="创建时间"
        align="center"
        prop="createTime"
        :formatter="dateFormatter"
        width="180px"
      />
      <el-table-column label="操作" align="center" min-width="120px">
        <template #default="scope">
          <el-button
            link
            type="primary"
            @click="openForm('update', scope.row.id)"
            v-hasPermi="['AiFriend:agent-info:update']"
          >
            编辑
          </el-button>
          <el-button
            link
            type="danger"
            @click="handleDelete(scope.row.id)"
            v-hasPermi="['AiFriend:agent-info:delete']"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页 -->
    <Pagination
      :total="total"
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
  </ContentWrap>

  <!-- 表单弹窗：添加/修改 -->
  <AgentInfoForm ref="formRef" @success="getList" />
</template>

<script setup lang="ts">
import { getStrDictOptions, getBoolDictOptions, DICT_TYPE } from '@/utils/dict'
import { isEmpty } from '@/utils/is'
import { dateFormatter } from '@/utils/formatTime'
import download from '@/utils/download'
import { AgentInfoApi, AgentInfo } from '@/api/AiFriend/config'
import AgentInfoForm from './AgentInfoForm.vue'

/** Agent 定义表 列表 */
defineOptions({ name: 'AgentInfo' })

const message = useMessage() // 消息弹窗
const { t } = useI18n() // 国际化

const loading = ref(true) // 列表的加载中
const list = ref<AgentInfo[]>([]) // 列表的数据
const total = ref(0) // 列表的总页数
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  id: undefined,
  modelId: undefined,
  sysPromptId: undefined,
  agentCode: undefined,
  agentName: undefined,
  description: undefined,
  maxIters: undefined,
  permissionMode: undefined,
  compactionEnabled: undefined,
  triggerMessages: undefined,
  keepMessages: undefined,
  triggerTokens: undefined,
  keepTokens: undefined,
  toolResultEvictionEnabled: undefined,
  memoryEnable: undefined,
  planModeEnabled: undefined,
  allowShellInPlanMode: undefined,
  createTime: []
})
const queryFormRef = ref() // 搜索的表单
const exportLoading = ref(false) // 导出的加载中

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await AgentInfoApi.getAgentInfoPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value.resetFields()
  handleQuery()
}

/** 添加/修改操作 */
const formRef = ref()
const openForm = (type: string, id?: number) => {
  formRef.value.open(type, id)
}

/** 删除按钮操作 */
const handleDelete = async (id: number) => {
  try {
    // 删除的二次确认
    await message.delConfirm()
    // 发起删除
    await AgentInfoApi.deleteAgentInfo(id)
    message.success(t('common.delSuccess'))
    // 刷新列表
    await getList()
  } catch {}
}

/** 批量删除Agent 定义表 */
const handleDeleteBatch = async () => {
  try {
    // 删除的二次确认
    await message.delConfirm()
    await AgentInfoApi.deleteAgentInfoList(checkedIds.value);
    checkedIds.value = [];
    message.success(t('common.delSuccess'))
    await getList();
  } catch {}
}

const checkedIds = ref<number[]>([])
const handleRowCheckboxChange = (records: AgentInfo[]) => {
  checkedIds.value = records.map((item) => item.id!);
}

/** 导出按钮操作 */
const handleExport = async () => {
  try {
    // 导出的二次确认
    await message.exportConfirm()
    // 发起导出
    exportLoading.value = true
    const data = await AgentInfoApi.exportAgentInfo(queryParams)
    download.excel(data, 'Agent 定义表.xls')
  } catch {
  } finally {
    exportLoading.value = false
  }
}

/** 初始化 **/
onMounted(() => {
  getList()
})
</script>
