/** 与后端 TerminalEnum 一一对应。 */
export const TerminalEnum = {
  UNKNOWN: 0,
  WECHAT_MINI_PROGRAM: 10,
  WECHAT_WAP: 11,
  H5: 20,
  APP: 31,
};

export function getTerminal() {
  const platform = uni.getAppBaseInfo?.().uniPlatform || uni.getSystemInfoSync().uniPlatform;

  switch (platform) {
    case 'app':
      return TerminalEnum.APP;
    case 'web':
      return TerminalEnum.H5;
    case 'mp-weixin':
      return TerminalEnum.WECHAT_MINI_PROGRAM;
    default:
      return TerminalEnum.UNKNOWN;
  }
}
