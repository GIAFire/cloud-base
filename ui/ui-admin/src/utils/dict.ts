/**
 * 数据字典工具类
 */
import { useDictStoreWithOut } from '@/store/modules/dict'
import { ElementPlusInfoType } from '@/types/elementPlus'

const dictStore = useDictStoreWithOut()

/**
 * 获取 dictType 对应的数据字典数组
 *
 * @param dictType 数据类型
 * @returns {*|Array} 数据字典数组
 */
export interface DictDataType {
  dictType: string
  label: string
  value: string | number | boolean
  colorType: ElementPlusInfoType | ''
  cssClass: string
}

export interface NumberDictDataType extends DictDataType {
  value: number
}

export interface StringDictDataType extends DictDataType {
  value: string
}

export interface BooleanDictDataType extends DictDataType {
  value: boolean
}

export const getDictOptions = (dictType: string) => {
  return dictStore.getDictByType(dictType) || []
}

export const getIntDictOptions = (dictType: string): NumberDictDataType[] => {
  // 获得通用的 DictDataType 列表
  const dictOptions: DictDataType[] = getDictOptions(dictType)
  // 转换成 number 类型的 NumberDictDataType 类型
  // why 需要特殊转换：避免 IDEA 在 v-for="dict in getIntDictOptions(...)" 时，el-option 的 key 会告警
  const dictOption: NumberDictDataType[] = []
  dictOptions.forEach((dict: DictDataType) => {
    dictOption.push({
      ...dict,
      value: parseInt(dict.value + '')
    })
  })
  return dictOption
}

export const getStrDictOptions = (dictType: string) => {
  // 获得通用的 DictDataType 列表
  const dictOptions: DictDataType[] = getDictOptions(dictType)
  // 转换成 string 类型的 StringDictDataType 类型
  // why 需要特殊转换：避免 IDEA 在 v-for="dict in getStrDictOptions(...)" 时，el-option 的 key 会告警
  const dictOption: StringDictDataType[] = []
  dictOptions.forEach((dict: DictDataType) => {
    dictOption.push({
      ...dict,
      value: dict.value + ''
    })
  })
  return dictOption
}

export const getBoolDictOptions = (dictType: string): BooleanDictDataType[] => {
  const dictOption: BooleanDictDataType[] = []
  const dictOptions: DictDataType[] = getDictOptions(dictType)
  dictOptions.forEach((dict: DictDataType) => {
    dictOption.push({
      ...dict,
      value: dict.value + '' === 'true'
    })
  })
  return dictOption
}

/**
 * 获取指定字典类型的指定值对应的字典对象
 * @param dictType 字典类型
 * @param value 字典值
 * @return DictDataType 字典对象
 */
export const getDictObj = (dictType: string, value: any): DictDataType | undefined => {
  const dictOptions: DictDataType[] = getDictOptions(dictType)
  for (const dict of dictOptions) {
    if (dict.value === value + '') {
      return dict
    }
  }
}

/**
 * 获得字典数据的文本展示
 *
 * @param dictType 字典类型
 * @param value 字典数据的值
 * @return 字典名称
 */
export const getDictLabel = (dictType: string, value: any): string => {
  const dictOptions: DictDataType[] = getDictOptions(dictType)
  const dictLabel = ref('')
  dictOptions.forEach((dict: DictDataType) => {
    if (dict.value === value + '') {
      dictLabel.value = dict.label
    }
  })
  return dictLabel.value
}

export enum DICT_TYPE {
  USER_TYPE = 'user_type',
  COMMON_STATUS = 'common_status',
  TERMINAL = 'terminal', // 终端
  DATE_INTERVAL = 'date_interval', // 数据间隔

  // ========== SYSTEM 模块 ==========
  SYSTEM_USER_SEX = 'system_user_sex',
  SYSTEM_MENU_TYPE = 'system_menu_type',
  SYSTEM_ROLE_TYPE = 'system_role_type',
  SYSTEM_DATA_SCOPE = 'system_data_scope',
  SYSTEM_NOTICE_TYPE = 'system_notice_type',
  SYSTEM_LOGIN_TYPE = 'system_login_type',
  SYSTEM_LOGIN_RESULT = 'system_login_result',
  SYSTEM_SMS_CHANNEL_CODE = 'system_sms_channel_code',
  SYSTEM_SMS_TEMPLATE_TYPE = 'system_sms_template_type',
  SYSTEM_SMS_SEND_STATUS = 'system_sms_send_status',
  SYSTEM_SMS_RECEIVE_STATUS = 'system_sms_receive_status',
  SYSTEM_OAUTH2_GRANT_TYPE = 'system_oauth2_grant_type',
  SYSTEM_MAIL_SEND_STATUS = 'system_mail_send_status',
  SYSTEM_NOTIFY_TEMPLATE_TYPE = 'system_notify_template_type',
  SYSTEM_SOCIAL_TYPE = 'system_social_type',

  // ========== INFRA 模块 ==========
  INFRA_BOOLEAN_STRING = 'infra_boolean_string',
  INFRA_JOB_STATUS = 'infra_job_status',
  INFRA_JOB_LOG_STATUS = 'infra_job_log_status',
  INFRA_API_ERROR_LOG_PROCESS_STATUS = 'infra_api_error_log_process_status',
  INFRA_CONFIG_TYPE = 'infra_config_type',
  INFRA_CODEGEN_TEMPLATE_TYPE = 'infra_codegen_template_type',
  INFRA_CODEGEN_FRONT_TYPE = 'infra_codegen_front_type',
  INFRA_CODEGEN_SCENE = 'infra_codegen_scene',
  INFRA_FILE_STORAGE = 'infra_file_storage',
  INFRA_OPERATE_TYPE = 'infra_operate_type',

  // ========== PAY 模块 ==========
  PAY_CHANNEL_CODE = 'pay_channel_code', // 支付渠道编码类型
  PAY_ORDER_STATUS = 'pay_order_status', // 商户支付订单状态
  PAY_REFUND_STATUS = 'pay_refund_status', // 退款订单状态
  PAY_NOTIFY_STATUS = 'pay_notify_status', // 商户支付回调状态
  PAY_NOTIFY_TYPE = 'pay_notify_type', // 商户支付回调状态
  PAY_TRANSFER_TYPE = 'pay_transfer_type', // 转账订单类型
  PAY_TRANSFER_STATUS = 'pay_transfer_status', // 转账订单状态

  // ========== Member 会员模块 ==========
  MEMBER_POINT_BIZ_TYPE = 'member_point_biz_type', // 积分的业务类型
  MEMBER_EXPERIENCE_BIZ_TYPE = 'member_experience_biz_type', // 会员经验业务类型

  // ========== IM - 即时通讯模块  ==========
  IM_CONTENT_TYPE = 'im_content_type', // IM 内容类型
  IM_MESSAGE_STATUS = 'im_message_status', // IM 消息状态：0=正常 / 2=已撤回（私聊 / 群聊共用）
  IM_MESSAGE_RECEIPT_STATUS = 'im_message_receipt_status', // IM 消息回执状态：0=不需要 / 1=待完成 / 2=已完成
  IM_FRIEND_STATUS = 'im_friend_status', // IM 好友状态
  IM_FRIEND_ADD_SOURCE = 'im_friend_add_source', // IM 好友添加来源
  IM_FRIEND_REQUEST_HANDLE_RESULT = 'im_friend_request_handle_result', // IM 好友申请处理结果
  IM_GROUP_STATUS = 'im_group_status', // IM 群状态
  IM_GROUP_MEMBER_ROLE = 'im_group_member_role', // IM 群成员角色
  IM_GROUP_ADD_SOURCE = 'im_group_add_source', // IM 加群来源
  IM_GROUP_REQUEST_HANDLE_RESULT = 'im_group_request_handle_result', // IM 加群申请处理结果
  IM_RTC_CALL_MEDIA_TYPE = 'im_rtc_call_media_type', // IM 通话媒体类型：1=语音 / 2=视频
  IM_RTC_CALL_CONVERSATION_TYPE = 'im_rtc_call_conversation_type', // IM 通话会话类型：1=私聊 / 2=群聊
  IM_RTC_CALL_STATUS = 'im_rtc_call_status', // IM 通话状态：10=创建 / 20=进行中 / 30=已结束
  IM_RTC_CALL_END_REASON = 'im_rtc_call_end_reason', // IM 通话结束原因：1=通话结束 / 2=已拒绝 / 3=已取消 / 4=无人接听 / 5=对方正忙 / 9=通话异常
  IM_RTC_PARTICIPANT_ROLE = 'im_rtc_participant_role', // IM 通话参与角色：1=发起人 / 2=被邀请者 / 3=主动加入者
  IM_RTC_PARTICIPANT_STATUS = 'im_rtc_participant_status', // IM 通话参与状态：10=邀请中 / 20=已加入 / 30=已拒绝 / 40=未应答 / 50=已离开
  IM_CHANNEL_MATERIAL_TYPE = 'im_channel_material_type' // IM 频道素材内容类型：1=富文本 / 2=外链
}
