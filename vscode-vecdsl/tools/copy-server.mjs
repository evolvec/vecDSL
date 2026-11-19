// Copies vecdsl-lsp.jar into server/ for packaging: the file named by VECDSL_LSP_JAR, else ../vecdsl-lsp/target/vecdsl-lsp.jar.
import { createHash } from 'node:crypto';
import { copyFileSync, existsSync, mkdirSync, readFileSync, statSync } from 'node:fs';
import { dirname, join, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const extension = resolve(dirname(fileURLToPath(import.meta.url)), '..');
const source = resolve(process.env.VECDSL_LSP_JAR ?? join(extension, '..', 'vecdsl-lsp', 'target', 'vecdsl-lsp.jar'));
if (!existsSync(source)) {
  console.error(`copy-server: ${source} does not exist; build vecdsl-lsp (mvn -q clean package) or set VECDSL_LSP_JAR`);
  process.exit(1);
}
const target = join(extension, 'server', 'vecdsl-lsp.jar');
mkdirSync(dirname(target), { recursive: true });
copyFileSync(source, target);
const md5 = createHash('md5').update(readFileSync(target)).digest('hex');
console.log(`copy-server: ${source} -> server/vecdsl-lsp.jar, ${statSync(target).size} bytes, md5 ${md5}`);
