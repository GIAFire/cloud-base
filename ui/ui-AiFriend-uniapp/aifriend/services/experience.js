import {characters} from '../domain/catalog.js';
import {emptyExperience, experienceRepository} from '../repositories/experience.js';

// The application boundary owns the persistence implementation. Stores and pages
// use this facade so a later API adapter can replace the local repository here.
export const experienceService = {
    restore: () => experienceRepository().load(),
    save: (data) => experienceRepository().save(data),
    clear: () => {
        experienceRepository().clear();
        return emptyExperience();
    },
};

export function discoverCharacters({query = '', tag = '全部', category = '为你推荐', favorites = []} = {}) {
    const needle = query.trim().toLocaleLowerCase();
    const result = characters.filter((person) =>
        (tag === '全部' || person.type === tag || person.tags.includes(tag)) &&
        (!needle || [person.name, person.type, ...person.tags].join(' ').toLocaleLowerCase().includes(needle)) &&
        (category !== '我的收藏' || favorites.includes(person.id)));
    if (category === '最新相遇') return result.reverse();
    if (category === '人气角色') return result.sort((a, b) => b.favorites - a.favorites);
    return result;
}

export function replyFor(message, preference) {
    if (preference === '安静陪伴') return '嗯，我在。你可以慢慢说，也可以和我安静地待一会儿。';
    if (preference === '轻松有趣') return /累|难过|烦/.test(message)
        ? '今天的烦恼先交给小岛保管吧。要不要一起想一件让你开心的小事？'
        : '收到你的消息，今日快乐值 +1！快告诉我，今天有什么新鲜事？';
    if (/累|难过|烦/.test(message)) return '听起来今天有点不容易。愿意告诉我发生了什么吗？我在这里陪着你。';
    if (/你好|嗨/.test(message)) return '终于等到你啦。今天有没有一个让你嘴角上扬的瞬间？';
    return '我听着呢。想再多了解一点你的想法，也想陪你把这一刻好好珍藏。';
}

export function validateAuth(form, mode, codeEmail) {
    const email = form.email.trim().toLowerCase();
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email) || email.length > 254) return '请输入有效的邮箱地址';
    if (form.password.length < 8 || form.password.length > 32) return '密码需为 8–32 位';
    if (mode === 'login') return '';
    if (codeEmail !== email) return '请先为当前邮箱获取演示验证码';
    if (form.code !== '123456') return '验证码不正确，演示验证码为 123456';
    if (!/[a-z]/i.test(form.password) || !/[0-9]/.test(form.password)) return '密码需同时包含字母和数字';
    if (form.confirm !== form.password) return '两次输入的密码不一致';
    if (mode === 'register' && !form.agreed) return '请确认用户协议、隐私政策及年满 18 周岁';
    return '';
}

export function safeReturnRoute(route) {
    return typeof route === 'string' && /^\/pages\/(home\/index|conversations\/index|video\/index|profile\/index|chat\/index)(\?[^#]*)?$/.test(route)
        ? route : '/pages/home/index';
}
