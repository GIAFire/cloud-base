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
      <el-form-item label="模型接口协议：openaiCompatible、dashscope、anthropic、ollama" prop="protocol">
        <el-input
          v-model="queryParams.protocol"
          placeholder="请输入模型接口协议：openaiCompatible、dashscope、anthropic、ollama"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="兼容 OpenAI 协议时的自定义 baseUrl" prop="baseUrl">
        <el-input
          v-model="queryParams.baseUrl"
          placeholder="请输入兼容 OpenAI 协议时的自定义 baseUrl"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="API Key；当前阶段明文存储，仅模型详情接口允许返回" prop="apiKey">
        <el-input
          v-model="queryParams.apiKey"
          placeholder="请输入API Key；当前阶段明文存储，仅模型详情接口允许返回"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="真实模型名称，例如 qwen-plus、gpt-4.1、claude-sonnet-4-5" prop="modelName">
        <el-input
          v-model="queryParams.modelName"
          placeholder="请输入真实模型名称，例如 qwen-plus、gpt-4.1、claude-sonnet-4-5"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="是否支持流式" prop="streaming">
        <el-select
          v-model="queryParams.streaming"
          placeholder="请选择是否支持流式"
          clearable
          class="!w-240px"
        >
          <el-option label="请选择字典生成" value="" />
        </el-select>
      </el-form-item>
      <el-form-item label="是否支持思考" prop="thinking">
        <el-select
          v-model="queryParams.thinking"
          placeholder="请选择是否支持思考"
          clearable
          class="!w-240px"
        >
          <el-option label="请选择字典生成" value="" />
        </el-select>
      </el-form-item>
      <el-form-item label="采样温度，控制随机性" prop="temperature">
        <el-input
          v-model="queryParams.temperature"
          placeholder="请输入采样温度，控制随机性"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="核采样参数" prop="topP">
        <el-input
          v-model="queryParams.topP"
          placeholder="请输入核采样参数"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="最大输出token" prop="maxTokens">
        <el-input
          v-model="queryParams.maxTokens"
          placeholder="请输入最大输出token"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="最大思考token" prop="thinkingBudget">
        <el-input
          v-model="queryParams.thinkingBudget"
          placeholder="请输入最大思考token"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
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
          v-hasPermi="['agent:model:create']"
        >
          <Icon icon="ep:plus" class="mr-5px" /> 新增
        </el-button>
        <el-button
          type="success"
          plain
          @click="handleExport"
          :loading="exportLoading"
          v-hasPermi="['agent:model:export']"
        >
          <Icon icon="ep:download" class="mr-5px" /> 导出
        </el-button>
        <el-button
            type="danger"
            plain
            :disabled="isEmpty(checkedIds)"
            @click="handleDeleteBatch"
            v-hasPermi="['agent:model:delete']"
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
      <el-table-column label="模型配置主键ID" align="center" prop="id" />
      <el-table-column label="模型接口协议：openaiCompatible、dashscope、anthropic、ollama" align="center" prop="protocol" />
      <el-table-column label="兼容 OpenAI 协议时的自定义 baseUrl" align="center" prop="baseUrl" />
      <el-table-column label="API Key；当前阶段明文存储，仅模型详情接口允许返回" align="center" prop="apiKey" />
      <el-table-column label="模型说明" align="center" prop="description" />
      <el-table-column label="真实模型名称，例如 qwen-plus、gpt-4.1、claude-sonnet-4-5" align="center" prop="modelName" />
      <el-table-column label="是否支持流式" align="center" prop="streaming" />
      <el-table-column label="是否支持思考" align="center" prop="thinking" />
      <el-table-column label="采样温度，控制随机性" align="center" prop="temperature" />
      <el-table-column label="核采样参数" align="center" prop="topP" />
      <el-table-column label="最大输出token" align="center" prop="maxTokens" />
      <el-table-column label="最大思考token" align="center" prop="thinkingBudget" />
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
            v-hasPermi="['agent:model:update']"
          >
            编辑
          </el-button>
          <el-button
            link
            type="danger"
            @click="handleDelete(scope.row.id)"
            v-hasPermi="['agent:model:delete']"
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
  <ModelForm ref="formRef" @success="getList" />
</template>

<script setup lang="ts">
import { isEmpty } from '@/utils/is'
import { dateFormatter } from '@/utils/formatTime'
import download from '@/utils/download'
import { ModelApi, Model } from '@/api/agent/config'
import ModelForm from './ModelForm.vue'

/** 模型配置表 列表 */
defineOptions({ name: 'Model' })

const message = useMessage() // 消息弹窗
const { t } = useI18n() // 国际化

const loading = ref(true) // 列表的加载中
const list = ref<Model[]>([]) // 列表的数据
const total = ref(0) // 列表的总页数
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  protocol: undefined,
  baseUrl: undefined,
  apiKey: undefined,
  description: undefined,
  modelName: undefined,
  streaming: undefined,
  thinking: undefined,
  temperature: undefined,
  topP: undefined,
  maxTokens: undefined,
  thinkingBudget: undefined,
  createTime: []
})
const queryFormRef = ref() // 搜索的表单
const exportLoading = ref(false) // 导出的加载中

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await ModelApi.getModelPage(queryParams)
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
    await ModelApi.deleteModel(id)
    message.success(t('common.delSuccess'))
    // 刷新列表
    await getList()
  } catch {}
}

/** 批量删除模型配置表 */
const handleDeleteBatch = async () => {
  try {
    // 删除的二次确认
    await message.delConfirm()
    await ModelApi.deleteModelList(checkedIds.value);
    checkedIds.value = [];
    message.success(t('common.delSuccess'))
    await getList();
  } catch {}
}

const checkedIds = ref<number[]>([])
const handleRowCheckboxChange = (records: Model[]) => {
  checkedIds.value = records.map((item) => item.id!);
}

/** 导出按钮操作 */
const handleExport = async () => {
  try {
    // 导出的二次确认
    await message.exportConfirm()
    // 发起导出
    exportLoading.value = true
    const data = await ModelApi.exportModel(queryParams)
    download.excel(data, '模型配置表.xls')
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