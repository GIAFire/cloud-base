/** ========== COMMON - 通用模块 ========== */
const COMMON_DICT = {
  USER_TYPE: 'user_type',
  COMMON_STATUS: 'common_status',
  TERMINAL: 'terminal', // 终端
  DATE_INTERVAL: 'date_interval', // 数据间隔
} as const

/** ========== SYSTEM - 系统模块 ========== */
const SYSTEM_DICT = {
  SYSTEM_USER_SEX: 'system_user_sex',
  SYSTEM_MENU_TYPE: 'system_menu_type',
  SYSTEM_ROLE_TYPE: 'system_role_type',
  SYSTEM_DATA_SCOPE: 'system_data_scope',
  SYSTEM_NOTICE_TYPE: 'system_notice_type',
  SYSTEM_LOGIN_TYPE: 'system_login_type',
  SYSTEM_LOGIN_RESULT: 'system_login_result',
  SYSTEM_SMS_CHANNEL_CODE: 'system_sms_channel_code',
  SYSTEM_SMS_TEMPLATE_TYPE: 'system_sms_template_type',
  SYSTEM_SMS_SEND_STATUS: 'system_sms_send_status',
  SYSTEM_SMS_RECEIVE_STATUS: 'system_sms_receive_status',
  SYSTEM_OAUTH2_GRANT_TYPE: 'system_oauth2_grant_type',
  SYSTEM_MAIL_SEND_STATUS: 'system_mail_send_status',
  SYSTEM_NOTIFY_TEMPLATE_TYPE: 'system_notify_template_type',
  SYSTEM_SOCIAL_TYPE: 'system_social_type',
  SYSTEM_DICT_COLOR_TYPE: 'system_dict_color_type', // 字典颜色类型
} as const

/** ========== INFRA - 基础设施模块 ========== */
const INFRA_DICT = {
  INFRA_BOOLEAN_STRING: 'infra_boolean_string',
  INFRA_JOB_STATUS: 'infra_job_status',
  INFRA_JOB_LOG_STATUS: 'infra_job_log_status',
  INFRA_API_ERROR_LOG_PROCESS_STATUS: 'infra_api_error_log_process_status',
  INFRA_CONFIG_TYPE: 'infra_config_type',
  INFRA_CODEGEN_TEMPLATE_TYPE: 'infra_codegen_template_type',
  INFRA_CODEGEN_FRONT_TYPE: 'infra_codegen_front_type',
  INFRA_CODEGEN_SCENE: 'infra_codegen_scene',
  INFRA_FILE_STORAGE: 'infra_file_storage',
  INFRA_OPERATE_TYPE: 'infra_operate_type',
} as const

/** ========== HRM - 人力资源模块 ========== */
const HRM_DICT = {
  HRM_EMPLOYEE_ENTRY_STATUS: 'hrm_employee_entry_status', // HRM 员工入职状态
  HRM_EMPLOYEE_STATUS: 'hrm_employee_status', // HRM 员工状态
  HRM_EMPLOYEE_TYPE: 'hrm_employee_type', // HRM 聘用形式
  HRM_EMPLOYEE_EDUCATION: 'hrm_employee_education', // HRM 员工学历
  HRM_RECRUIT_CANDIDATE_STATUS: 'hrm_recruit_candidate_status', // 招聘候选人状态
  HRM_RECRUIT_CANDIDATE_EDUCATION: 'hrm_recruit_candidate_education', // 招聘候选人学历
  HRM_RECRUIT_INTERVIEW_TYPE: 'hrm_recruit_interview_type', // 招聘面试方式
  HRM_RECRUIT_INTERVIEW_RESULT: 'hrm_recruit_interview_result', // 招聘面试结果
  HRM_RECRUIT_POST_STATUS: 'hrm_recruit_post_status', // 招聘职位状态
  HRM_RECRUIT_JOB_NATURE: 'hrm_recruit_job_nature', // 招聘工作性质
  HRM_RECRUIT_WORK_TIME: 'hrm_recruit_work_time', // 招聘工作经验
  HRM_RECRUIT_POST_EDUCATION: 'hrm_recruit_post_education', // 招聘职位学历要求
  HRM_RECRUIT_SALARY_UNIT: 'hrm_recruit_salary_unit', // 招聘薪资单位
  HRM_RECRUIT_EMERGENCY_LEVEL: 'hrm_recruit_emergency_level', // 招聘紧急程度
  HRM_ATTENDANCE_CLOCK_TYPE: 'hrm_attendance_clock_type', // HRM 打卡类型
  HRM_ATTENDANCE_CLOCK_SOURCE: 'hrm_attendance_clock_source', // HRM 打卡来源
  HRM_ATTENDANCE_CLOCK_STATUS: 'hrm_attendance_clock_status', // HRM 打卡状态
  HRM_ATTENDANCE_LEAVE_TYPE: 'hrm_attendance_leave_type', // HRM 请假类型
  HRM_ATTENDANCE_APPROVAL_STATUS: 'bpm_process_instance_status', // HRM 请假审批状态
  HRM_ATTENDANCE_YES_NO: 'hrm_attendance_yes_no', // HRM 考勤是否
  HRM_ATTENDANCE_HOLIDAY_TYPE: 'hrm_attendance_holiday_type', // HRM 考勤节假日类型
  HRM_ATTENDANCE_LATE_EARLY_DEDUCT_METHOD: 'hrm_attendance_late_early_deduct_method', // HRM 迟到早退扣款方式
  HRM_ATTENDANCE_ABSENTEEISM_DEDUCT_METHOD: 'hrm_attendance_absenteeism_deduct_method', // HRM 旷工扣款方式
  HRM_ATTENDANCE_MISSCARD_DEDUCT_METHOD: 'hrm_attendance_misscard_deduct_method', // HRM 缺卡扣款方式
  HRM_INSURANCE_SCHEME_TYPE: 'hrm_insurance_scheme_type', // HRM 社保方案类型
  HRM_INSURANCE_PROJECT_TYPE: 'hrm_insurance_project_type', // HRM 社保项目类型
  HRM_INSURANCE_MONTH_STATUS: 'hrm_insurance_month_status', // HRM 社保表状态
  HRM_INSURANCE_EMP_STATUS: 'hrm_insurance_emp_status', // HRM 参保状态
  HRM_SALARY_MONTH_STATUS: 'hrm_salary_month_status', // HRM 工资表状态
  HRM_SALARY_CHANGE_TYPE: 'hrm_salary_change_type', // HRM 薪资档案状态
  HRM_SALARY_CHANGE_REASON: 'hrm_salary_change_reason', // HRM 调薪原因
  HRM_SALARY_CHANGE_RECORD_STATUS: 'hrm_salary_change_record_status', // HRM 调薪记录状态
  HRM_SALARY_SLIP_READ_STATUS: 'hrm_salary_slip_read_status', // HRM 工资条阅读状态
  HRM_SALARY_TAX_TYPE: 'hrm_salary_tax_type', // HRM 薪资计税类型
  HRM_SALARY_OPTION_TYPE: 'hrm_salary_option_type', // HRM 薪资项类型
  HRM_SALARY_YES_NO: 'hrm_salary_yes_no', // HRM 薪资是否
  HRM_PERFORMANCE_PLAN_STATUS: 'hrm_performance_plan_status', // HRM 绩效计划状态
  HRM_PERFORMANCE_STAGE_STATUS: 'hrm_performance_stage_status', // HRM 绩效阶段状态
  HRM_PERFORMANCE_ASSESSMENT_STAGE_STATUS: 'hrm_performance_assessment_stage_status', // HRM 绩效考核阶段处理状态
  HRM_PERFORMANCE_APPEAL_STATUS: 'hrm_performance_appeal_status', // HRM 绩效申诉状态
  HRM_PERFORMANCE_SCORE_CALCULATION: 'hrm_performance_score_calculation', // HRM 绩效计分方式
  HRM_PERFORMANCE_UPPER_LIMIT_TYPE: 'hrm_performance_upper_limit_type', // HRM 绩效上限类型
  HRM_PERFORMANCE_YES_NO: 'hrm_performance_yes_no', // HRM 绩效是否
} as const

/** ========== MEMBER - 会员模块 ========== */
const MEMBER_DICT = {
  MEMBER_POINT_BIZ_TYPE: 'member_point_biz_type', // 会员积分业务类型
  MEMBER_EXPERIENCE_BIZ_TYPE: 'member_experience_biz_type', // 会员经验业务类型
} as const

/** ========== PAY - 支付模块 ========== */
const PAY_DICT = {
  PAY_CHANNEL_CODE: 'pay_channel_code', // 支付渠道编码
  PAY_ORDER_STATUS: 'pay_order_status', // 商户支付订单状态
  PAY_REFUND_STATUS: 'pay_refund_status', // 退款订单状态
  PAY_NOTIFY_STATUS: 'pay_notify_status', // 商户支付回调状态
  PAY_NOTIFY_TYPE: 'pay_notify_type', // 商户支付回调类型
  PAY_TRANSFER_STATUS: 'pay_transfer_status', // 转账订单状态
  PAY_TRANSFER_TYPE: 'pay_transfer_type', // 转账类型
} as const

/** ========== IM - 即时通讯模块 ========== */
const IM_DICT = {
  IM_CONTENT_TYPE: 'im_content_type', // IM 消息内容类型
  IM_MESSAGE_STATUS: 'im_message_status', // IM 消息状态（私聊 / 群聊共用）
  IM_MESSAGE_RECEIPT_STATUS: 'im_message_receipt_status', // IM 消息回执状态（私聊 / 群聊共用）
  IM_FRIEND_STATUS: 'im_friend_status', // IM 好友状态
  IM_FRIEND_ADD_SOURCE: 'im_friend_add_source', // IM 好友添加来源
  IM_FRIEND_REQUEST_HANDLE_RESULT: 'im_friend_request_handle_result', // IM 好友申请处理结果
  IM_GROUP_STATUS: 'im_group_status', // IM 群状态
  IM_GROUP_MEMBER_ROLE: 'im_group_member_role', // IM 群成员角色
  IM_GROUP_ADD_SOURCE: 'im_group_add_source', // IM 加群来源
  IM_GROUP_REQUEST_HANDLE_RESULT: 'im_group_request_handle_result', // IM 加群申请处理结果
  IM_RTC_CALL_MEDIA_TYPE: 'im_rtc_call_media_type', // IM 通话媒体类型
  IM_RTC_CALL_CONVERSATION_TYPE: 'im_rtc_call_conversation_type', // IM 通话会话类型
  IM_RTC_CALL_STATUS: 'im_rtc_call_status', // IM 通话状态
  IM_RTC_CALL_END_REASON: 'im_rtc_call_end_reason', // IM 通话结束原因
  IM_RTC_PARTICIPANT_ROLE: 'im_rtc_participant_role', // IM 通话参与角色
  IM_RTC_PARTICIPANT_STATUS: 'im_rtc_participant_status', // IM 通话参与状态
  IM_CHANNEL_MATERIAL_TYPE: 'im_channel_material_type', // IM 频道素材内容类型
} as const

/** 字典类型枚举 - 统一导出 */
export const DICT_TYPE = {
  ...IM_DICT,
  ...HRM_DICT,
  ...PAY_DICT,
  ...MEMBER_DICT,
  ...INFRA_DICT,
  ...SYSTEM_DICT,
  ...COMMON_DICT,
} as const
