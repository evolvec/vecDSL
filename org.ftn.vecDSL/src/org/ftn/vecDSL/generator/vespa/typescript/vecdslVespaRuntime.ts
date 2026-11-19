/**
 * Runtime support for vecDSL programs generated for Vespa, over its HTTP APIs with the fetch of Node.js. Generated
 * by vecDSL 7. Do not edit.
 *
 * Every statement arrives as application package files, YQL texts and query request bodies built by the generator, identical in
 * the Python, TypeScript and Java programs; values travel as request parameters that the YQL references with @name, never inside
 * the YQL text. DDL statements deploy the whole package through the deploy API of the config server and wait until the
 * application has converged and serves queries; writes go through document/v1 with the document id id:vecdsl:<type>::<key>;
 * reads use the query API.
 */
import { createHash } from "node:crypto";
import { readFileSync } from "node:fs";

const NAMESPACE = "vecdsl";
const CLUSTER = "vecdsl";
const EXPIRES = "vecdsl_expires";
const VERSION = "vecdsl_version";
const NO_EXPIRY = 253402300799;
const DIGITS = 6;
const PAGE = 1000;
const HIGHLIGHT = "vecdsl_hl_";
const DEPLOY_TIMEOUT = 600;
const READY_TIMEOUT = 300;
const POLL = 2;
const RETRY_TIMEOUT = 60;
const UNTIL = "{" + "{until}}";
const APPLICATION = "/application/v2/tenant/default/application/default";
const INSTANCE = APPLICATION + "/environment/prod/region/default/instance/default";

type Json = null | boolean | number | string | Json[] | { [key: string]: Json };
type Fields = Record<string, Json>;
type Hit = Record<string, unknown>;
type Body = Record<string, unknown>;

export class UnsupportedFeature extends Error {}

export class ConflictError extends Error {}

export class VespaError extends Error {
  constructor(message: string, readonly status?: number) {
    super(message);
  }
}

export function unsupported(vendor: string, feature: string): never {
  throw new UnsupportedFeature(`${feature} is not supported for target ${vendor}`);
}

export function env(name: string): string {
  const value = process.env[name];
  if (value === undefined) {
    throw new Error(`environment variable ${name} is not set`);
  }
  return value;
}

// ---------------------------------------------------------------- values

/** A number as text in one form for every host language: integers without a fraction, others in plain decimal notation. */
export function plain(value: number): string {
  if (!Number.isFinite(value)) {
    throw new Error("JSON has no infinite or NaN numbers");
  }
  if (Number.isInteger(value) && Math.abs(value) < 1e15) {
    return String(value);
  }
  const text = String(value);
  const match = /^(-?)(\d+)(?:\.(\d+))?e([+-]\d+)$/.exec(text);
  if (match === null) {
    return text;
  }
  const [, sign, whole, fraction = "", exponentText] = match;
  const exponent = Number(exponentText);
  const digits = whole + fraction;
  const point = whole.length + exponent;
  let result: string;
  if (point <= 0) {
    result = "0." + "0".repeat(-point) + digits;
  } else if (point >= digits.length) {
    result = digits + "0".repeat(point - digits.length);
  } else {
    result = digits.slice(0, point) + "." + digits.slice(point);
  }
  result = result.includes(".") ? result.replace(/0+$/, "").replace(/\.$/, "") : result;
  return sign + result.replace(/^0+(?=\d)/, "");
}

/** Compact JSON with numbers written by plain(), so that the three runtimes send identical bodies. */
export function dumps(value: unknown): string {
  if (value === null || value === undefined) {
    return "null";
  }
  if (typeof value === "boolean") {
    return value ? "true" : "false";
  }
  if (typeof value === "number") {
    return plain(value);
  }
  if (typeof value === "string") {
    return JSON.stringify(value);
  }
  if (Array.isArray(value)) {
    return "[" + value.map(dumps).join(",") + "]";
  }
  if (typeof value === "object") {
    return "{" + Object.entries(value as Record<string, unknown>).map(([key, item]) => JSON.stringify(key) + ":" + dumps(item)).join(",") + "}";
  }
  throw new TypeError(`cannot write ${typeof value} as JSON`);
}

function normalize(value: unknown): unknown {
  if (typeof value === "number") {
    return Number.isInteger(value) ? value : Number(value.toFixed(DIGITS));
  }
  if (Array.isArray(value)) {
    return value.map(normalize);
  }
  if (value !== null && typeof value === "object") {
    return Object.fromEntries(Object.entries(value as Record<string, unknown>).map(([key, item]) => [key, normalize(item)]));
  }
  return value;
}

export function keyText(identifier: unknown): string {
  if (typeof identifier === "number") {
    return plain(identifier);
  }
  if (typeof identifier === "string") {
    return identifier;
  }
  throw new Error(`invalid record key ${JSON.stringify(identifier)}`);
}

function order(identifier: unknown): [number, number | string] {
  return typeof identifier === "number" ? [0, identifier] : [1, String(identifier)];
}

function compareIds(left: unknown, right: unknown): number {
  const [a, b] = [order(left), order(right)];
  if (a[0] !== b[0]) {
    return a[0] - b[0];
  }
  if (typeof a[1] === "number" && typeof b[1] === "number") {
    return a[1] - b[1];
  }
  return a[1] < b[1] ? -1 : a[1] > b[1] ? 1 : 0;
}

function sleep(seconds: number): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, seconds * 1000));
}

function now(): number {
  return Math.floor(Date.now() / 1000);
}

function monotonic(): number {
  return performance.now() / 1000;
}

// ---------------------------------------------------------------- connection

/** The query and document API (url) and the config server (configUrl) of one Vespa application; an optional bearer token. */
export class Client {
  constructor(readonly url: string, readonly configUrl: string, readonly token: string | undefined, readonly timeout: number) {}

  /** One HTTP request; returns the status and the decoded JSON (or text) of the answer, for every status. */
  async request(method: string, base: string, path: string, query?: Record<string, unknown>, body?: Uint8Array | string,
    contentType = "application/json", timeout?: number): Promise<[number, unknown]> {
    let address = base + path;
    if (query !== undefined && Object.keys(query).length > 0) {
      address += "?" + new URLSearchParams(Object.entries(query).map(([name, item]): [string, string] => [name, parameter(item)])).toString();
    }
    const headers: Record<string, string> = {};
    if (body !== undefined) {
      headers["Content-Type"] = contentType;
    }
    if (this.token) {
      headers["Authorization"] = "Bearer " + this.token;
    }
    let response: Response;
    try {
      response = await fetch(address, { method, headers, body, signal: AbortSignal.timeout((timeout ?? this.timeout) * 1000) });
    } catch (error) {
      throw new VespaError(`${method} ${address} failed: ${String(error)}`);
    }
    return [response.status, decode(await response.text())];
  }

  async close(): Promise<void> {}
}

function parameter(value: unknown): string {
  if (typeof value === "boolean") {
    return value ? "true" : "false";
  }
  return typeof value === "number" ? plain(value) : String(value);
}

function decode(text: string): unknown {
  if (text.trim() === "") {
    return null;
  }
  try {
    return JSON.parse(text);
  } catch {
    return text;
  }
}

export async function connect(args: { url: string; configUrl: string; tokenVariable?: string; timeout?: number }): Promise<Client> {
  const token = process.env[args.tokenVariable ?? "VECDSL_VESPA_TOKEN"];
  return new Client(args.url.replace(/\/+$/, ""), args.configUrl.replace(/\/+$/, ""), token ? token : undefined, args.timeout ?? 30);
}

/** A request repeated while the server answers 429 or 503 (too many operations, or a cluster still starting), at most 60 s. */
async function retried(client: Client, method: string, base: string, path: string, query?: Record<string, unknown>,
  body?: Uint8Array | string, contentType = "application/json", timeout?: number): Promise<[number, unknown]> {
  const deadline = monotonic() + RETRY_TIMEOUT;
  let delay = 0.2;
  for (;;) {
    const [status, answer] = await client.request(method, base, path, query, body, contentType, timeout);
    if ((status !== 429 && status !== 503) || monotonic() > deadline) {
      return [status, answer];
    }
    await sleep(delay);
    delay = Math.min(delay * 2, 2);
  }
}

function fail(what: string, status: number, answer: unknown): never {
  const message = answer !== null && typeof answer === "object" && "message" in answer ? (answer as { message: unknown }).message : answer;
  throw new VespaError(`${what} failed with HTTP ${status}: ${typeof message === "string" ? message : JSON.stringify(message)}`, status);
}

function record(value: unknown): Record<string, unknown> {
  return value !== null && typeof value === "object" && !Array.isArray(value) ? (value as Record<string, unknown>) : {};
}

// ---------------------------------------------------------------- deployment

/** The document types of the active application (its schema files), or null without an application. */
async function deployed(client: Client): Promise<string[] | null> {
  let [status, answer] = await client.request("GET", client.configUrl, "/application/v2/tenant/default/application/");
  if (status !== 200) {
    fail("listing the applications", status, answer);
  }
  if (!Array.isArray(answer) || answer.length === 0) {
    return null;
  }
  [status, answer] = await client.request("GET", client.configUrl, INSTANCE + "/content/schemas/");
  if (status === 404) {
    return [];
  }
  if (status !== 200 || !Array.isArray(answer)) {
    fail("reading the deployed schemas", status, answer);
  }
  const names = (answer as string[]).map((entry) => entry.replace(/\/+$/, "").split("/").pop() ?? "");
  return names.filter((name) => name.endsWith(".sd")).map((name) => name.slice(0, -3)).sort();
}

const CRC_TABLE = Array.from({ length: 256 }, (_, n) => {
  let c = n;
  for (let k = 0; k < 8; k += 1) {
    c = c & 1 ? 0xedb88320 ^ (c >>> 1) : c >>> 1;
  }
  return c >>> 0;
});

function crc32(data: Uint8Array): number {
  let c = 0xffffffff;
  for (const byte of data) {
    c = CRC_TABLE[(c ^ byte) & 0xff] ^ (c >>> 8);
  }
  return (c ^ 0xffffffff) >>> 0;
}

/** The package as a zip archive with fixed timestamps, stored (not compressed). */
function zip(files: Record<string, string>): Uint8Array {
  const parts: Buffer[] = [];
  const central: Buffer[] = [];
  let offset = 0;
  for (const [name, text] of Object.entries(files)) {
    const nameBytes = Buffer.from(name, "utf8");
    const data = Buffer.from(text, "utf8");
    const checksum = crc32(data);
    const local = Buffer.alloc(30);
    local.writeUInt32LE(0x04034b50, 0);
    local.writeUInt16LE(20, 4);
    local.writeUInt16LE(0x0800, 6);
    local.writeUInt16LE(0, 8);
    local.writeUInt16LE(0, 10);
    local.writeUInt16LE(0x0021, 12);
    local.writeUInt32LE(checksum, 14);
    local.writeUInt32LE(data.length, 18);
    local.writeUInt32LE(data.length, 22);
    local.writeUInt16LE(nameBytes.length, 26);
    local.writeUInt16LE(0, 28);
    const entry = Buffer.alloc(46);
    entry.writeUInt32LE(0x02014b50, 0);
    entry.writeUInt16LE(0x0314, 4);
    entry.writeUInt16LE(20, 6);
    entry.writeUInt16LE(0x0800, 8);
    entry.writeUInt16LE(0, 10);
    entry.writeUInt16LE(0, 12);
    entry.writeUInt16LE(0x0021, 14);
    entry.writeUInt32LE(checksum, 16);
    entry.writeUInt32LE(data.length, 20);
    entry.writeUInt32LE(data.length, 24);
    entry.writeUInt16LE(nameBytes.length, 28);
    entry.writeUInt32LE((0o100644 << 16) >>> 0, 38);
    entry.writeUInt32LE(offset, 42);
    parts.push(local, nameBytes, data);
    central.push(entry, nameBytes);
    offset += local.length + nameBytes.length + data.length;
  }
  const directory = Buffer.concat(central);
  const end = Buffer.alloc(22);
  end.writeUInt32LE(0x06054b50, 0);
  end.writeUInt16LE(Object.keys(files).length, 8);
  end.writeUInt16LE(Object.keys(files).length, 10);
  end.writeUInt32LE(directory.length, 12);
  end.writeUInt32LE(offset, 16);
  return new Uint8Array(Buffer.concat([...parts, directory, end]));
}

export interface DeployArgs {
  files: Record<string, string>;
  overrides?: string;
  created?: string[];
  skip_existing?: boolean;
  dropped?: string[];
  ignore_missing?: boolean;
}

/**
 * Deploys the package as the program's statements leave it. A created type that is already deployed fails the statement (or,
 * with skip_existing, leaves the application as it is); a dropped type that is not deployed fails it unless ignore_missing; the
 * documents of a deployed dropped type are deleted first. No files: the application is removed.
 */
export async function deploy(client: Client, args: DeployArgs): Promise<void> {
  const present = await deployed(client);
  const types = present ?? [];
  for (const name of args.created ?? []) {
    if (types.includes(name)) {
      if (args.skip_existing) {
        return;
      }
      throw new ConflictError(`collection ${name} already exists`);
    }
  }
  for (const name of args.dropped ?? []) {
    if (!types.includes(name) && !args.ignore_missing) {
      throw new VespaError(`collection ${name} does not exist`);
    }
  }
  for (const name of args.dropped ?? []) {
    if (types.includes(name)) {
      await deleteAll(client, name);
    }
  }
  if (Object.keys(args.files).length === 0) {
    if (present !== null) {
      const [status, answer] = await client.request("DELETE", client.configUrl, APPLICATION, undefined, undefined, undefined, DEPLOY_TIMEOUT);
      if (status !== 200 && status !== 404) {
        fail("removing the application", status, answer);
      }
    }
    return;
  }
  const packageFiles: Record<string, string> = { ...args.files };
  if (args.overrides !== undefined) {
    const until = new Date(Date.now() + 86_400_000).toISOString().slice(0, 10);
    packageFiles["validation-overrides.xml"] = args.overrides.split(UNTIL).join(until);
  }
  const [status, answer] = await client.request("POST", client.configUrl, "/application/v2/tenant/default/prepareandactivate", undefined,
    zip(packageFiles), "application/zip", DEPLOY_TIMEOUT);
  if (status !== 200) {
    fail("deploying the application package", status, answer);
  }
  const session = Number(record(answer)["session-id"]);
  await awaitConverged(client, session, monotonic() + DEPLOY_TIMEOUT);
  const schemaTypes = Object.keys(args.files).filter((name) => name.startsWith("schemas/") && name.endsWith(".sd"))
    .map((name) => name.slice("schemas/".length, -3));
  await awaitServing(client, schemaTypes, monotonic() + READY_TIMEOUT);
}

/** Polls serviceconverge until every service runs the deployed session's config generation. */
async function awaitConverged(client: Client, session: number, deadline: number): Promise<void> {
  for (;;) {
    const [status, answer] = await client.request("GET", client.configUrl, INSTANCE + "/serviceconverge");
    const state = record(answer);
    if (status === 200 && state["converged"] === true && Number(state["currentGeneration"] ?? 0) >= session) {
      return;
    }
    if (monotonic() > deadline) {
      throw new Error(`the application did not converge on session ${session} within ${DEPLOY_TIMEOUT} s (serviceconverge: ${JSON.stringify(answer)})`);
    }
    await sleep(POLL);
  }
}

/** Waits until the container answers /status.html and each document type answers a query and a document lookup. */
async function awaitServing(client: Client, types: string[], deadline: number): Promise<void> {
  const pending = ["/status.html", ...types];
  while (pending.length > 0) {
    const target = pending[0];
    let ready = false;
    let answer: unknown = null;
    try {
      if (target === "/status.html") {
        const [status, text] = await client.request("GET", client.url, "/status.html");
        [ready, answer] = [status === 200, text];
      } else {
        const [status, result] = await client.request("POST", client.url, "/search/", undefined,
          dumps({ yql: `select * from ${target} where true`, hits: 0 }));
        answer = result;
        ready = status === 200 && !record(record(result)["root"])["errors"];
        if (ready) {
          const [probe, found] = await client.request("GET", client.url, path(target, "vecdsl-readiness-probe"), { "format.tensors": "short-value" });
          [ready, answer] = [probe === 200 || probe === 404, found];
        }
      }
    } catch (error) {
      [ready, answer] = [false, String(error)];
    }
    if (ready) {
      pending.shift();
      continue;
    }
    if (monotonic() > deadline) {
      throw new Error(`the application did not serve ${target} within ${READY_TIMEOUT} s (last answer: ${JSON.stringify(answer)})`);
    }
    await sleep(POLL);
  }
}

/** Waits until the deployment has converged and the document type serves queries, at most timeout seconds. */
export async function awaitReady(client: Client, args: { document_type: string; timeout?: number }): Promise<unknown> {
  const deadline = monotonic() + (args.timeout ?? READY_TIMEOUT);
  const [status, answer] = await client.request("GET", client.configUrl, APPLICATION);
  if (status !== 200) {
    fail("reading the application", status, answer);
  }
  await awaitConverged(client, Number(record(answer)["generation"] ?? 0), deadline);
  await awaitServing(client, [args.document_type], deadline);
  return { collection: args.document_type, state: "ready" };
}

export async function requireCollection(client: Client, args: { document_type: string }): Promise<void> {
  if (!((await deployed(client)) ?? []).includes(args.document_type)) {
    throw new VespaError(`collection ${args.document_type} does not exist`);
  }
}

/** Triggers reindexing of the document type and polls the reindexing status until it succeeds, at most timeout seconds. */
export async function reindex(client: Client, args: { document_type: string; timeout?: number }): Promise<unknown> {
  await requireCollection(client, { document_type: args.document_type });
  const timeout = args.timeout ?? DEPLOY_TIMEOUT;
  let [status, answer] = await client.request("POST", client.configUrl, INSTANCE + "/reindex", { clusterId: CLUSTER, documentType: args.document_type });
  if (status !== 200) {
    fail("triggering reindexing", status, answer);
  }
  const deadline = monotonic() + timeout;
  for (;;) {
    [status, answer] = await client.request("GET", client.configUrl, INSTANCE + "/reindexing");
    const state = status === 200 ? record(record(record(record(record(answer)["clusters"])[CLUSTER])["ready"])[args.document_type])["state"] : undefined;
    if (state === "successful") {
      return { collection: args.document_type, state };
    }
    if (state === "failed") {
      throw new VespaError(`reindexing ${args.document_type} failed: ${JSON.stringify(answer)}`);
    }
    if (monotonic() > deadline) {
      throw new Error(`reindexing ${args.document_type} did not finish within ${timeout} s (state ${String(state)})`);
    }
    await sleep(POLL);
  }
}

/** The deployed schema of the collection, its document count on request, and whether the deployment has converged. */
export async function describe(client: Client, args: { document_type: string; stats?: Search; progress?: boolean }): Promise<unknown> {
  const [status, answer] = await client.request("GET", client.configUrl, INSTANCE + `/content/schemas/${args.document_type}.sd`);
  if (status !== 200) {
    fail(`reading the schema of ${args.document_type}`, status, answer);
  }
  const result: Record<string, unknown> = { name: args.document_type, schema: answer };
  if (args.stats !== undefined) {
    result["count"] = await count(client, args.stats);
  }
  if (args.progress) {
    const [converge, state] = await client.request("GET", client.configUrl, INSTANCE + "/serviceconverge");
    result["converged"] = converge === 200 && record(state)["converged"] === true;
  }
  return result;
}

/** collections and databases (the deployed schemas), the program's index names, and the values of a slice attribute (grouping). */
export async function names(client: Client, args: {
  kind: string; prefix?: string; limit?: number; database?: string; names?: string[]; body?: Body;
}): Promise<string[]> {
  let found: string[];
  if (args.kind === "collections" || args.kind === "databases") {
    const types = (await deployed(client)) ?? [];
    if (args.kind === "databases") {
      found = [...new Set(types.filter((name) => name.includes("_")).map((name) => name.split("_", 1)[0]))];
    } else if (args.database !== undefined) {
      const database = args.database;
      found = types.filter((name) => name.startsWith(database + "_")).map((name) => name.slice(database.length + 1));
    } else {
      found = [...types];
    }
  } else if (args.kind === "indexes") {
    found = [...(args.names ?? [])];
  } else {
    const root = await query(client, { body: args.body ?? {} });
    found = [];
    for (const groupList of (root["children"] as Array<Record<string, unknown>> | undefined) ?? []) {
      for (const listNode of (groupList["children"] as Array<Record<string, unknown>> | undefined) ?? []) {
        for (const group of (listNode["children"] as Array<Record<string, unknown>> | undefined) ?? []) {
          // a document without the slice has the empty string in the attribute, which is not a slice name
          if (group["value"] !== undefined && group["value"] !== null && group["value"] !== "") {
            found.push(String(group["value"]));
          }
        }
      }
    }
  }
  found = found.filter((name) => args.prefix === undefined || name.startsWith(args.prefix)).sort();
  return args.limit === undefined ? found : found.slice(0, args.limit);
}

// ---------------------------------------------------------------- documents

function path(documentType: string, key: unknown): string {
  return `/document/v1/${NAMESPACE}/${documentType}/docid/` + encodeURIComponent(keyText(key));
}

function fieldsOf(row: Fields, key: string, vectors?: Record<string, number>, slices?: Fields): Fields {
  if (row[key] === null || row[key] === undefined) {
    throw new Error(`a record without its key field ${key}`);
  }
  const fields: Fields = {};
  for (const [name, item] of Object.entries(row)) {
    if (item !== null && item !== undefined) {
      fields[name] = item;
    }
  }
  for (const [name, dim] of Object.entries(vectors ?? {})) {
    const vector = fields[name];
    if (Array.isArray(vector) && vector.length !== dim) {
      throw new Error(`vector field ${name} has ${vector.length} dimensions, the schema expects ${dim}`);
    }
  }
  return { ...fields, ...(slices ?? {}) };
}

async function getFields(client: Client, documentType: string, key: unknown): Promise<Fields | null> {
  const [status, answer] = await retried(client, "GET", client.url, path(documentType, key), { "format.tensors": "short-value" });
  if (status === 404) {
    return null;
  }
  if (status !== 200) {
    fail(`reading document ${keyText(key)}`, status, answer);
  }
  return (record(answer)["fields"] ?? {}) as Fields;
}

function expired(fields: Fields, at: number): boolean {
  const value = fields[EXPIRES];
  return typeof value === "number" && value <= at;
}

export interface WriteArgs {
  document_type: string;
  key: string;
  records: Fields[];
  policy: string;
  counter: string;
  vectors?: Record<string, number>;
  slices?: Fields;
  expiring?: boolean;
  ttl?: number;
  versioned?: boolean;
}

/**
 * Puts each record as a document. error: every id is read first and any existing one fails the write before anything is written;
 * error and ignore then put with the condition false and create (an existing document answers 412); replace puts unconditionally.
 */
export async function write(client: Client, args: WriteArgs): Promise<Record<string, number>> {
  const at = now();
  const documents: Array<[Json, Fields]> = [];
  for (const row of args.records) {
    const fields = fieldsOf(row, args.key, args.vectors, args.slices);
    if (args.expiring) {
      fields[EXPIRES] = args.ttl !== undefined ? at + args.ttl : NO_EXPIRY;
    }
    if (args.versioned) {
      fields[VERSION] = 1;
    }
    documents.push([row[args.key], fields]);
  }
  if (args.policy === "error") {
    const existing: string[] = [];
    for (const [identifier] of documents) {
      const found = await getFields(client, args.document_type, identifier);
      if (found !== null && !expired(found, at)) {
        existing.push(keyText(identifier));
      }
    }
    if (existing.length > 0) {
      throw new ConflictError(`records with these ids exist already: ${existing.join(", ")}; nothing was written`);
    }
  }
  let written = 0;
  for (const [identifier, fields] of documents) {
    const operation = args.policy === "error" || args.policy === "ignore" ? { condition: "false", create: true, fields } : { fields };
    const [status, answer] = await retried(client, "POST", client.url, path(args.document_type, identifier), undefined, dumps(operation));
    if (status === 412) {
      if (args.policy === "ignore") {
        continue;
      }
      throw new ConflictError(`record ${keyText(identifier)} exists already`);
    }
    if (status !== 200) {
      fail(`writing document ${keyText(identifier)}`, status, answer);
    }
    written += 1;
  }
  return { [args.counter]: written };
}

export async function readRecords(_client: Client, args: { location: string }): Promise<Fields[]> {
  const file = args.location.startsWith("file://") ? args.location.slice("file://".length) : args.location;
  const text = readFileSync(file, "utf8");
  if (text.trimStart().startsWith("[")) {
    return JSON.parse(text) as Fields[];
  }
  return text.split(/\r?\n/).filter((line) => line.trim() !== "").map((line) => JSON.parse(line) as Fields);
}

export async function importFile(client: Client, args: Omit<WriteArgs, "records"> & { location: string }): Promise<Record<string, number>> {
  return write(client, { ...args, records: await readRecords(client, { location: args.location }) });
}

export interface Search {
  body: Body;
  texts?: Record<string, unknown>;
  numbers?: Record<string, unknown>;
  flags?: Record<string, unknown>;
  clock?: boolean;
  metric?: string;
  feature?: string;
  native?: boolean;
  lookup?: Lookup;
  scores?: boolean;
}

export interface Lookup {
  document_type: string;
  field: string;
  positive_ids?: unknown[];
  negative_ids?: unknown[];
  positive_vectors?: number[][];
  negative_vectors?: number[][];
}

/** The keys of the documents a query selects, in key order: every page, or the first limit keys. */
async function selected(client: Client, key: string, select: Search, limit?: number): Promise<unknown[]> {
  const keys: unknown[] = [];
  for (;;) {
    const wanted = limit === undefined ? PAGE : Math.min(PAGE, limit - keys.length);
    const root = await query(client, { ...select, body: { ...select.body, hits: wanted, offset: keys.length } });
    const found = hitsOf(root).map((hit) => record(hit["fields"])[key]);
    keys.push(...found);
    if (found.length < wanted || (limit !== undefined && keys.length >= limit)) {
      return keys;
    }
  }
}

export interface UpdateArgs {
  document_type: string;
  key: string;
  ids?: unknown[];
  select?: Search;
  records?: Fields[];
  set?: Fields;
  append?: Record<string, Json[]>;
  remove?: Record<string, Json[]>;
  clear?: string[];
  vectors?: Record<string, number>;
  version?: number;
  versioned?: boolean;
}

/** Partial updates by id, each conditional on the document existing (or on its version), so missing documents are not counted. */
export async function update(client: Client, args: UpdateArgs): Promise<Record<string, number>> {
  const condition = args.version !== undefined ? `${args.document_type}.${VERSION}==${args.version}` : args.document_type;
  const changes: Array<[unknown, Record<string, unknown>]> = [];
  if (args.records !== undefined) {
    for (const row of args.records) {
      const fields = fieldsOf(row, args.key, args.vectors);
      const operations: Record<string, unknown> = {};
      for (const [name, item] of Object.entries(fields)) {
        if (name !== args.key) {
          operations[name] = { assign: item };
        }
      }
      changes.push([row[args.key], operations]);
    }
  } else {
    const operations: Record<string, unknown> = {};
    for (const [name, item] of Object.entries(args.set ?? {})) {
      const dim = (args.vectors ?? {})[name];
      if (dim !== undefined && Array.isArray(item) && item.length !== dim) {
        throw new Error(`vector field ${name} has ${item.length} dimensions, the schema expects ${dim}`);
      }
      operations[name] = { assign: item };
    }
    for (const [name, items] of Object.entries(args.append ?? {})) {
      operations[name] = { add: items };
    }
    for (const [name, items] of Object.entries(args.remove ?? {})) {
      operations[name] = { remove: items };
    }
    for (const name of args.clear ?? []) {
      operations[name] = { assign: null };
    }
    const targets = args.ids !== undefined ? [...args.ids] : await selected(client, args.key, args.select ?? { body: {} });
    for (const identifier of targets) {
      changes.push([identifier, operations]);
    }
  }
  let updated = 0;
  for (const [identifier, operations] of changes) {
    const fields: Record<string, unknown> = { ...operations };
    if (args.versioned) {
      fields[VERSION] = { increment: 1 };
    }
    const [status, answer] = await retried(client, "PUT", client.url, path(args.document_type, identifier), undefined,
      dumps({ condition, fields }));
    if (status === 412) {
      continue;
    }
    if (status !== 200) {
      fail(`updating document ${keyText(identifier)}`, status, answer);
    }
    updated += 1;
  }
  return { update_count: updated };
}

/** Removes documents by id (conditional on the type, so missing ids are not counted), selected by a query, or every document. */
async function delete_(client: Client, args: {
  document_type: string; key: string; ids?: unknown[]; select?: Search; limit?: number; all?: boolean;
}): Promise<Record<string, number>> {
  if (args.all) {
    return { delete_count: await deleteAll(client, args.document_type) };
  }
  const targets = args.ids !== undefined ? [...args.ids] : await selected(client, args.key, args.select ?? { body: {} }, args.limit);
  let deleted = 0;
  for (const identifier of targets) {
    const [status, answer] = await retried(client, "DELETE", client.url, path(args.document_type, identifier), { condition: args.document_type });
    if (status === 412) {
      continue;
    }
    if (status !== 200) {
      fail(`deleting document ${keyText(identifier)}`, status, answer);
    }
    deleted += 1;
  }
  return { delete_count: deleted };
}
export { delete_ as delete };

/** Delete where the selection is true over the type's documents, chunk by chunk; returns how many there were. */
async function deleteAll(client: Client, documentType: string): Promise<number> {
  const total = await count(client, { body: { yql: `select * from ${documentType} where true`, hits: 0 } });
  let continuation: string | undefined;
  for (;;) {
    const params: Record<string, unknown> = { selection: "true", cluster: CLUSTER };
    if (continuation) {
      params["continuation"] = continuation;
    }
    const [status, answer] = await retried(client, "DELETE", client.url, `/document/v1/${NAMESPACE}/${documentType}/docid`, params,
      undefined, undefined, DEPLOY_TIMEOUT);
    if (status !== 200) {
      fail(`deleting the documents of ${documentType}`, status, answer);
    }
    const next = record(answer)["continuation"];
    continuation = typeof next === "string" && next !== "" ? next : undefined;
    if (continuation === undefined) {
      return total;
    }
  }
}

/** Sets the expiry time of every existing document (an update where the selection is true); returns how many there were. */
export async function expire(client: Client, args: { document_type: string; seconds: number }): Promise<Record<string, number>> {
  const total = await count(client, { body: { yql: `select * from ${args.document_type} where true`, hits: 0 } });
  const body = dumps({ fields: { [EXPIRES]: { assign: now() + args.seconds } } });
  let continuation: string | undefined;
  for (;;) {
    const params: Record<string, unknown> = { selection: "true", cluster: CLUSTER };
    if (continuation) {
      params["continuation"] = continuation;
    }
    const [status, answer] = await retried(client, "PUT", client.url, `/document/v1/${NAMESPACE}/${args.document_type}/docid`, params,
      body, undefined, DEPLOY_TIMEOUT);
    if (status !== 200) {
      fail(`setting the expiry of ${args.document_type}`, status, answer);
    }
    const next = record(answer)["continuation"];
    continuation = typeof next === "string" && next !== "" ? next : undefined;
    if (continuation === undefined) {
      return { expire_count: total };
    }
  }
}

/** Documents by id, in the order of the ids; missing documents, documents outside the slices and expired ones are skipped. */
export async function get(client: Client, args: {
  document_type: string; key: string; ids: unknown[]; output: string[]; vectors?: string[]; slices?: Record<string, Json[]>; expiring?: boolean;
}): Promise<Hit[]> {
  const at = now();
  const hits: Hit[] = [];
  for (const identifier of args.ids) {
    const fields = await getFields(client, args.document_type, identifier);
    if (fields === null || (args.expiring && expired(fields, at))) {
      continue;
    }
    if (Object.entries(args.slices ?? {}).some(([name, allowed]) => !allowed.includes(fields[name] ?? null))) {
      continue;
    }
    hits.push(project(fields, args.key, args.output, args.vectors ?? [], null));
  }
  return hits;
}

// ---------------------------------------------------------------- queries

function project(fields: Fields, key: string, output: string[], vectors: string[], score: number | null, highlight?: string[],
  explanation?: unknown): Hit {
  const hit: Hit = { id: fields[key] ?? null };
  if (score !== null) {
    hit["score"] = score;
  }
  for (const name of [...output, ...vectors]) {
    hit[name] = fields[name] ?? null;
  }
  if (highlight !== undefined && highlight.length > 0) {
    const marked: Record<string, unknown> = {};
    for (const name of highlight) {
      const value = fields[HIGHLIGHT + name];
      if (marked_(value)) {
        marked[name] = [value];
      }
    }
    hit["highlight"] = marked;
  }
  if (explanation !== undefined && explanation !== null) {
    hit["explanation"] = explanation;
  }
  return normalize(hit) as Hit;
}

/** POST /search/ with the generated body and the bound values, each checked against the type its @reference expects. */
async function query(client: Client, search: Search): Promise<Record<string, unknown>> {
  const request: Body = { ...search.body };
  for (const [name, item] of Object.entries(search.texts ?? {})) {
    if (typeof item !== "string") {
      throw new TypeError(`query parameter ${name} must be a string, not ${JSON.stringify(item)}`);
    }
    request[name] = item;
  }
  for (const [name, item] of Object.entries(search.numbers ?? {})) {
    if (typeof item !== "number" || !Number.isFinite(item)) {
      throw new TypeError(`query parameter ${name} must be a finite number, not ${JSON.stringify(item)}`);
    }
    request[name] = item;
  }
  for (const [name, item] of Object.entries(search.flags ?? {})) {
    if (typeof item !== "boolean") {
      throw new TypeError(`query parameter ${name} must be a boolean, not ${JSON.stringify(item)}`);
    }
    request[name] = item;
  }
  if (search.clock) {
    request["vecdsl_now"] = now();
  }
  const [status, answer] = await retried(client, "POST", client.url, "/search/", undefined, dumps(request));
  const root = record(record(answer)["root"]);
  if (status !== 200 || root["errors"] !== undefined) {
    fail("the query", status, root["errors"] !== undefined ? root["errors"] : answer);
  }
  return root;
}

/** A highlight fragment counts only with a bolded term: Vespa also returns the bolded summary field, unmarked, in hits that matched no term. */
function marked_(text: unknown): boolean {
  return typeof text === "string" && text.includes("<hi>");
}

function hitsOf(root: Record<string, unknown>): Array<Record<string, unknown>> {
  const children = (root["children"] as Array<Record<string, unknown>> | undefined) ?? [];
  // A hit filled from a document summary without documentid has an id index:<cluster>/<part>/<gid>, not id:...; only grouping nodes are left out.
  return children.filter((child) => "fields" in child && !String(child["id"] ?? "").startsWith("group:"));
}

async function vectorOf(client: Client, documentType: string, field: string, identifier: unknown): Promise<number[]> {
  const fields = await getFields(client, documentType, identifier);
  const vector = fields === null ? null : fields[field];
  if (!Array.isArray(vector)) {
    throw new Error(`no vector ${field} stored for id ${keyText(identifier)} in ${documentType}`);
  }
  return vector as number[];
}

/** The body with input.query(vecdsl_q): given, a stored document's vector, or 2 avg(positive) - avg(negative) of the examples. */
async function queryVector(client: Client, body: Body, lookup?: Lookup): Promise<Body> {
  if (lookup === undefined) {
    return body;
  }
  const positives = [...(lookup.positive_vectors ?? [])];
  const negatives = [...(lookup.negative_vectors ?? [])];
  for (const identifier of lookup.positive_ids ?? []) {
    positives.push(await vectorOf(client, lookup.document_type, lookup.field, identifier));
  }
  for (const identifier of lookup.negative_ids ?? []) {
    negatives.push(await vectorOf(client, lookup.document_type, lookup.field, identifier));
  }
  if (positives.length === 0) {
    throw new Error("no positive example to search with");
  }
  const average = (vectors: number[][]) => vectors[0].map((_, i) => vectors.reduce((sum, vector) => sum + vector[i], 0) / vectors.length);
  let mean = average(positives);
  if (negatives.length > 0) {
    const negative = average(negatives);
    mean = mean.map((value, i) => 2 * value - negative[i]);
  }
  return { ...body, input: { ...record(body["input"]), "query(vecdsl_q)": mean } };
}

/** Canonical similarity from a Vespa distance: cosine from the angle, the negated distance for euclidean and hamming, dot from -dot. */
function similarity(distance: number, metric: string | undefined): number {
  return metric === "cosine" ? Math.cos(distance) : -distance;
}

/** The distance from the relevance of a hit ranked by a generated vecdsl_nn_ profile (first phase closeness = 1 / (1 + distance)).
 *  Grouping hits (output(summary())) carry no match features, only the relevance. */
function closenessDistance(hit: Record<string, unknown>, search: Search): number | undefined {
  const profile = String(record(record(search.body)["ranking"])["profile"] ?? "");
  const relevance = hit["relevance"];
  if (!profile.startsWith("vecdsl_nn_") || !["cosine", "euclidean", "hamming"].includes(search.metric ?? "")
      || typeof relevance !== "number" || relevance <= 0) {
    return undefined;
  }
  return 1 / relevance - 1;
}

/** The canonical score of a hit (from the distance match feature), the native relevance, or no score. */
function scoreOf(hit: Record<string, unknown>, search: Search): number | null {
  if (search.scores === false) {
    return null;
  }
  if (search.feature !== undefined && !search.native) {
    const found = record(record(hit["fields"])["matchfeatures"])[search.feature];
    const value = typeof found === "number" ? found : closenessDistance(hit, search);
    if (typeof value !== "number") {
      throw new VespaError(`hit ${String(hit["id"])} has no match feature ${search.feature}`);
    }
    return similarity(value, search.metric);
  }
  return Number(hit["relevance"] ?? 0);
}

async function rootOf(client: Client, search: Search): Promise<Record<string, unknown>> {
  const body = await queryVector(client, search.body, search.lookup);
  return query(client, { ...search, body });
}

/** The hits of one query, best first, with their canonical score (or the native relevance), or no score. */
async function scored(client: Client, search: Search): Promise<Array<[Fields, number | null]>> {
  return hitsOf(await rootOf(client, search)).map((hit): [Fields, number | null] => [record(hit["fields"]) as Fields, scoreOf(hit, search)]);
}

function childrenOf(node: Record<string, unknown>): Array<Record<string, unknown>> {
  return (node["children"] as Array<Record<string, unknown>> | undefined) ?? [];
}

/** The groups of a query whose YQL ends in all(group(f) max(n) each(max(m) each(output(summary())))), in Vespa's order. */
async function groupsOf(client: Client, search: Search): Promise<Array<[Json, Array<[Fields, number | null]>]>> {
  const groups: Array<[Json, Array<[Fields, number | null]>]> = [];
  for (const top of childrenOf(await rootOf(client, search))) {
    if (!String(top["id"] ?? "").startsWith("group:root")) {
      continue;
    }
    for (const groupList of childrenOf(top)) {
      for (const group of childrenOf(groupList)) {
        const hits: Array<[Fields, number | null]> = [];
        for (const hitList of childrenOf(group)) {
          for (const hit of childrenOf(hitList)) {
            if ("fields" in hit) {
              hits.push([record(hit["fields"]) as Fields, scoreOf(hit, search)]);
            }
          }
        }
        groups.push([(group["value"] ?? null) as Json, hits]);
      }
    }
  }
  return groups;
}

function compareValues(left: unknown, right: unknown): number {
  if (typeof left === "string" && typeof right === "string") {
    return left < right ? -1 : left > right ? 1 : 0;
  }
  if (typeof left === "string" || typeof right === "string") {
    return typeof left === "string" ? 1 : -1;
  }
  return Number(left) - Number(right);
}

/** Hits ordered by the sort keys (null names the score), missing values last, the earlier order breaking ties. */
function sortedHits(found: Array<[Fields, number | null]>, order: Array<[string | null, string]>): Array<[Fields, number | null]> {
  const value = (item: [Fields, number | null], field: string | null): unknown => (field === null ? item[1] : item[0][field]);
  return [...found].sort((left, right) => {
    for (const [field, direction] of order) {
      const [a, b] = [value(left, field), value(right, field)];
      const missingA = a === undefined || a === null;
      const missingB = b === undefined || b === null;
      if (missingA || missingB) {
        if (missingA !== missingB) {
          return missingA ? 1 : -1;
        }
        continue;
      }
      const difference = compareValues(a, b) * (direction === "desc" ? -1 : 1);
      if (difference !== 0) {
        return difference;
      }
    }
    return 0;
  });
}

/** Slice i of n: the first 28 bits of the md5 of the key text, modulo n, are i (the same slices on every target). */
function inSlice(identifier: unknown, part: [number, number]): boolean {
  const digest = createHash("md5").update(keyText(identifier), "utf8").digest("hex");
  return parseInt(digest.slice(0, 7), 16) % part[1] === part[0];
}

/** Pages of a query in result order: every page, or the hits after the cursor record and in the slice, offset and limit applied. */
async function stream(client: Client, args: SearchArgs): Promise<Array<[Fields, number | null]>> {
  const size = Number(args.body["hits"]);
  const plainPages = args.after === undefined && args.slice === undefined;
  let start = plainPages ? Number(args.body["offset"] ?? 0) : 0;
  let passed = args.after === undefined;
  const offset = args.offset ?? 0;
  const found: Array<[Fields, number | null]> = [];
  for (;;) {
    const batch = await scored(client, { ...args, body: { ...args.body, offset: start } });
    start += batch.length;
    for (const [fields, score] of batch) {
      if (!passed) {
        passed = keyText(fields[args.key]) === keyText(args.after);
      } else if (args.slice === undefined || inSlice(fields[args.key], args.slice)) {
        found.push([fields, score]);
      }
    }
    if (batch.length < size || (!args.every && args.limit !== undefined && found.length >= offset + args.limit)) {
      break;
    }
  }
  if (plainPages) {
    return found;
  }
  return args.limit === undefined ? found.slice(offset) : found.slice(offset, offset + args.limit);
}

function within(score: number | null, low?: number, high?: number): boolean {
  return score === null || ((low === undefined || score >= low) && (high === undefined || score <= high));
}

export interface SearchArgs extends Search {
  key: string;
  output: string[];
  vectors?: string[];
  low?: number;
  high?: number;
  total?: Search;
  every?: boolean;
  after?: unknown;
  slice?: [number, number];
  order?: Array<[string | null, string]>;
  groups?: { field?: string; size?: number; offset?: number };
  highlight?: string[];
  limit?: number;
  offset?: number;
}

/**
 * A query built by the generator: the hits (id, score, output fields, vector fields), or {hits, total} with total; every: all pages;
 * after: the hits after the cursor record; slice: [i, n]; order: sort keys over the selected hits; groups: Vespa's groups.
 */
export async function search(client: Client, args: SearchArgs): Promise<unknown> {
  const hitOf = ([fields, score]: [Fields, number | null]) =>
    project(fields, args.key, args.output, args.vectors ?? [], score, args.highlight, fields["rankfeatures"]);
  let result: unknown[];
  if (args.groups !== undefined) {
    result = (await groupsOf(client, args)).slice(args.groups.offset ?? 0).map(([value, hits]) => ({
      group: normalize(value), hits: hits.filter(([, score]) => within(score, args.low, args.high)).map(hitOf),
    }));
  } else {
    let found = args.every || args.after !== undefined || args.slice !== undefined ? await stream(client, args) : await scored(client, args);
    found = found.filter(([, score]) => within(score, args.low, args.high));
    if (args.order !== undefined && args.order.length > 0) {
      found = sortedHits(found, args.order);
    }
    result = found.map(hitOf);
  }
  return args.total === undefined ? result : { hits: result, total: await count(client, args.total) };
}

/**
 * Legs queried one by one and fused as vecDSL defines rrf (sum of 1/(k + rank)), weighted and relative_score; ties by key. after: the hits
 * after the cursor record; groups: the fused hits grouped by a field; explain: each leg's rank features; total: {hits, total}.
 */
export async function hybrid(client: Client, args: {
  legs: Search[]; fusion: string; key: string; output: string[]; limit: number; offset?: number; k?: number; weights?: number[];
  vectors?: string[]; low?: number; high?: number; after?: unknown; order?: Array<[string | null, string]>;
  groups?: { field: string; size?: number }; highlight?: string[]; explain?: boolean; total?: Search;
}): Promise<unknown> {
  const weights = args.weights ?? args.legs.map(() => 1);
  const k = args.k ?? 60;
  const fused = new Map<string, number>();
  const documents = new Map<string, Fields>();
  const explained = new Map<string, Json[]>();
  for (let index = 0; index < args.legs.length; index += 1) {
    let values = (await scored(client, args.legs[index])).map(([fields, score]): [Fields, number] => [fields, score ?? 0]);
    if (args.fusion === "relative_score" && values.length > 0) {
      const least = Math.min(...values.map(([, value]) => value));
      const most = Math.max(...values.map(([, value]) => value));
      values = values.map(([fields, value]): [Fields, number] => [fields, most === least ? 1 : (value - least) / (most - least)]);
    }
    values.forEach(([fields, value], position) => {
      const name = keyText(fields[args.key]);
      const merged = documents.get(name) ?? {};
      for (const [field, item] of Object.entries(fields)) {
        // the keyword leg's bolded fragment wins over the vector leg's unmarked one
        if (!(field in merged) || (field.startsWith(HIGHLIGHT) && marked_(item))) {
          merged[field] = item;
        }
      }
      documents.set(name, merged);
      if (args.explain) {
        const legs = explained.get(name) ?? args.legs.map((): Json => null);
        legs[index] = fields["rankfeatures"] ?? null;
        explained.set(name, legs);
      }
      const part = args.fusion === "rrf" ? 1 / (k + position + 1) : weights[index] * value;
      fused.set(name, (fused.get(name) ?? 0) + part);
    });
  }
  let ranked = [...documents.keys()].sort((left, right) => {
    const difference = (fused.get(right) ?? 0) - (fused.get(left) ?? 0);
    return difference !== 0 ? difference : compareIds(documents.get(left)?.[args.key], documents.get(right)?.[args.key]);
  }).filter((name) => {
    const value = fused.get(name) ?? 0;
    return (args.low === undefined || value >= args.low) && (args.high === undefined || value <= args.high);
  });
  if (args.after !== undefined) {
    const position = ranked.indexOf(keyText(args.after));
    ranked = position < 0 ? [] : ranked.slice(position + 1);
  }
  const hitOf = (name: string) => project(documents.get(name) ?? {}, args.key, args.output, args.vectors ?? [], fused.get(name) ?? 0,
    args.highlight, explained.get(name));
  const offset = args.offset ?? 0;
  let result: unknown[];
  if (args.groups !== undefined) {
    const members = new Map<string, string[]>();
    const labels = new Map<string, Json>();
    for (const name of ranked) {
      const label = (documents.get(name) ?? {})[args.groups.field];
      if (label === undefined || label === null) {
        continue;
      }
      const group = dumps(label);
      if (!labels.has(group)) {
        labels.set(group, label);
      }
      const names = members.get(group) ?? [];
      if (names.length < (args.groups.size ?? 1)) {
        names.push(name);
      }
      members.set(group, names);
    }
    result = [...members.entries()].map(([group, names]) => ({ group: normalize(labels.get(group) ?? null), hits: names.map(hitOf) }))
      .slice(offset, offset + args.limit);
  } else {
    let page = ranked.slice(offset, offset + args.limit);
    if (args.order !== undefined && args.order.length > 0) {
      page = sortedHits(page.map((name): [Fields, number | null] => [documents.get(name) ?? {}, fused.get(name) ?? 0]), args.order)
        .map(([fields]) => keyText(fields[args.key]));
    }
    result = page.map(hitOf);
  }
  return args.total === undefined ? result : { hits: result, total: await count(client, args.total) };
}

/** The totalCount of a query without hits (exact for filters; a count never uses nearestNeighbor). */
export async function count(client: Client, args: Search): Promise<number> {
  const root = await query(client, args);
  return Number(record(root["fields"])["totalCount"] ?? 0);
}

/** Each sampled record with its nearest neighbours among the sample (canonical similarity, computed in the runtime). */
export async function distanceMatrix(client: Client, args: Search & { key: string; field: string; metric: string; limit: number }): Promise<unknown> {
  const sample = (await scored(client, { ...args, scores: false })).map(([fields]) => fields)
    .filter((fields) => Array.isArray(fields[args.field]) && (fields[args.field] as Json[]).length > 0);
  const pairs: Hit[] = [];
  for (const left of sample) {
    const scores: Array<[number, Fields]> = [];
    for (const right of sample) {
      if (right === left) {
        continue;
      }
      const u = left[args.field] as number[];
      const v = right[args.field] as number[];
      let value: number;
      if (args.metric === "euclidean") {
        value = -Math.sqrt(u.reduce((sum, a, i) => sum + (a - v[i]) ** 2, 0));
      } else if (args.metric === "dot") {
        value = u.reduce((sum, a, i) => sum + a * v[i], 0);
      } else if (args.metric === "hamming") {
        value = -u.reduce((sum, a, i) => sum + ((a ^ v[i]) & 0xff).toString(2).split("").filter((bit) => bit === "1").length, 0);
      } else {
        const norms = Math.sqrt(u.reduce((sum, a) => sum + a * a, 0)) * Math.sqrt(v.reduce((sum, b) => sum + b * b, 0));
        value = norms === 0 ? 0 : u.reduce((sum, a, i) => sum + a * v[i], 0) / norms;
      }
      scores.push([value, right]);
    }
    scores.sort((a, b) => (b[0] - a[0]) || compareIds(a[1][args.key], b[1][args.key]));
    for (const [value, right] of scores.slice(0, args.limit)) {
      pairs.push({ a: left[args.key] ?? null, b: right[args.key] ?? null, score: value });
    }
  }
  return normalize(pairs);
}
