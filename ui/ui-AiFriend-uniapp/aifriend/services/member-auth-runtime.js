import AuthApi from '../api/auth.js';
import UserApi from '../api/user.js';
import * as tokens from '../helper/token.js';
import { createMemberAuthService } from './member-auth.js';

export default createMemberAuthService({ authApi: AuthApi, userApi: UserApi, tokens });
