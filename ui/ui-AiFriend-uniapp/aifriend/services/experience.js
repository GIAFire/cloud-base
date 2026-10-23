import { emptyExperience, experienceRepository } from '../repositories/experience.js';
import { memberAuthService } from './member-auth.js';

export class FeatureUnavailableError extends Error {
  constructor(feature) {
    super(`${feature}暂不可用，请稍后再试`);
    this.name = 'FeatureUnavailableError';
  }
}
const unavailable = (feature) => async () => { throw new FeatureUnavailableError(feature); };

// Member authentication uses real APIs. Other business features await integration.
export const experienceService = {
  restore: () => experienceRepository().load(),
  save: (data) => experienceRepository().save(data),
  clear: () => { experienceRepository().clear(); return emptyExperience(); },
  ...memberAuthService,
  sendMessage: unavailable('消息发送'),
  addComment: unavailable('留言'),
  updateInteraction: unavailable('此操作'),
  reservePlus: unavailable('预约'),
  inviteVideo: unavailable('视频邀请'),
};
export function discoverCharacters({ characters = [], query = '', tag = '全部', category = '为你推荐', favorites = [] } = {}) {
  const needle = query.trim().toLocaleLowerCase();
  const result = characters.filter((person) =>
    (tag === '全部' || person.type === tag || person.tags?.includes(tag)) &&
    (!needle || [person.name, person.type, ...(person.tags || [])].join(' ').toLocaleLowerCase().includes(needle)) &&
    (category !== '我的收藏' || favorites.includes(person.id)));
  if (category === '最新相遇') return result.reverse();
  if (category === '人气角色') return result.sort((a, b) => (b.favorites || 0) - (a.favorites || 0));
  return result;
}
export function validateAuth(form, mode) {
  const email = form.email.trim().toLowerCase();
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email) || email.length > 100) return '请输入有效的邮箱地址（最多 100 位）';
  if (mode === 'login') return !form.password || form.password.length > 32 ? '请输入密码（最多 32 位）' : '';
  if (form.password.length < 8 || form.password.length > 32) return '密码需为 8–32 位';
  if (!/^[0-9]{6}$/.test(form.code)) return '请输入 6 位邮箱验证码';
  if (form.confirm !== form.password) return '两次输入的密码不一致';
  if (mode === 'register' && !form.agreed) return '请确认用户协议、隐私政策及年满 18 周岁';
  return '';
}
export function safeReturnRoute(route) {
  return typeof route === 'string' && /^\/pages\/(home\/index|conversations\/index|video\/index|profile\/index|chat\/index)(\?[^#]*)?$/.test(route)
    ? route : '/pages/home/index';
}
