import {spawnSync} from 'node:child_process';
import path from 'node:path';
import process from 'node:process';

const cliPath = path.resolve('node_modules/@dcloudio/vite-plugin-uni/bin/uni.js');
const result = spawnSync(process.execPath, [cliPath, ...process.argv.slice(2)], {
    cwd: process.cwd(),
    env: {
        ...process.env,
        // ui-mall-uniapp 使用根目录作为源码目录；CLI 默认查找 src，因此在这里显式对齐。
        UNI_INPUT_DIR: process.cwd(),
    },
    stdio: 'inherit',
});

if (result.error) {
    throw result.error;
}

process.exitCode = result.status ?? 1;
