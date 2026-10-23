// Kept independent of the transport so endpoint contracts can be verified.
export function createMemberAuthApi(request) {
  const post = (url, data, loadingMsg) => request({
    url: `/member/auth/${url}`, method: 'POST', data,
    custom: { auth: false, isToken: false, publicAuth: true, skipRefresh: true,
      showError: false, showSuccess: false, loadingMsg },
  });
  return {
    emailLogin: (data) => post('email-login', data, '登录中'),
    sendEmailCode: (email, scene) => post('email-code/send', { email, scene }, '发送中'),
    emailRegister: (data) => post('email-register', data, '注册中'),
    resetPasswordByEmail: (data) => post('email-reset-password', data, '修改中'),
  };
}
