export const MEMBER_STORAGE_KEY = 'aifriend:member';
export const MEMBER_CHANGED_EVENT = 'aifriend:member-changed';

function publicMember(member) {
  if (member?.id == null) return null;
  return { id: member.id, email: typeof member.email === 'string' ? member.email : '',
    nickname: typeof member.nickname === 'string' ? member.nickname : '',
    avatar: typeof member.avatar === 'string' ? member.avatar : '' };
}
export function createTokenStorage(storage) {
  return {
    getAccessToken: () => storage.getStorageSync('token'),
    getRefreshToken: () => storage.getStorageSync('refresh-token'),
    setToken(accessToken, refreshToken) {
      storage.setStorageSync('token', accessToken);
      storage.setStorageSync('refresh-token', refreshToken);
    },
    getMember() {
      if (!storage.getStorageSync('token')) return null;
      try {
        const raw = storage.getStorageSync(MEMBER_STORAGE_KEY);
        return publicMember(typeof raw === 'string' ? JSON.parse(raw) : raw);
      } catch { return null; }
    },
    setMember(value) {
      const member = publicMember(value);
      if (!member) throw new Error('账号信息不完整');
      storage.setStorageSync(MEMBER_STORAGE_KEY, JSON.stringify(member));
      storage.$emit?.(MEMBER_CHANGED_EVENT, member);
    },
    clearToken() {
      storage.removeStorageSync('token');
      storage.removeStorageSync('refresh-token');
      storage.removeStorageSync(MEMBER_STORAGE_KEY);
      storage.$emit?.(MEMBER_CHANGED_EVENT, null);
    },
  };
}
export const getAccessToken = () => createTokenStorage(uni).getAccessToken();
export const getRefreshToken = () => createTokenStorage(uni).getRefreshToken();
export const setToken = (access, refresh) => createTokenStorage(uni).setToken(access, refresh);
export const clearToken = () => createTokenStorage(uni).clearToken();
export const getMember = () => createTokenStorage(uni).getMember();
export const setMember = (member) => createTokenStorage(uni).setMember(member);
