const test = require('node:test');
const assert = require('node:assert/strict');
const { createService: createSession } = require('../getSessionId/services/rekognition');
const { createService: createResults } = require('../getSessionResults/services/rekognition');
const ID = '7bf3053e-ce07-4d45-80a2-e5c8e336d7a4';

test('crea una sesión idempotente sin imágenes de auditoría', async () => {
  const service = createSession(async command => {
    assert.deepEqual(command.input, { ClientRequestToken: ID, Settings: { AuditImagesLimit: 0 } });
    return { SessionId: ID };
  });
  assert.deepEqual(await service(ID), { sessionId: ID });
});

test('rechaza identificadores inválidos antes de contactar AWS', async () => {
  let calls = 0;
  const send = async () => { calls++; };
  for (const input of [undefined, '', 'otro', {}, '../secreto']) {
    await assert.rejects(createSession(send)(input));
    await assert.rejects(createResults(send)(input));
  }
  assert.equal(calls, 0);
});

test('consulta selfie solo para backend sin auditoría ni metadatos', async () => {
  const service = createResults(async command => {
    assert.deepEqual(command.input, { SessionId: ID });
    return { Status: 'SUCCEEDED', Confidence: 98.5, ReferenceImage: { Bytes: Buffer.from([255,216,255,0]) }, AuditImages: ['private'] };
  });
  assert.deepEqual(await service(ID), { status: 'SUCCEEDED', confidence: 98.5, referenceBase64: '/9j/AA==' });
});

test('un resultado en curso no implica aprobación', async () => {
  assert.deepEqual(await createResults(async () => ({ Status: 'IN_PROGRESS' }))(ID),
    { status: 'IN_PROGRESS', confidence: null, referenceBase64: null });
});

const { createService: compareService } = require('../compareFaces/services/rekognition');
const IMAGE = Buffer.from([255,216,255,0]).toString('base64');
test('compareFaces usa bytes y devuelve solo la similitud más alta', async () => {
  const service = compareService(async command => {
    assert.equal(command.input.SimilarityThreshold, 0);
    assert.deepEqual(command.input.SourceImage.Bytes, Buffer.from(IMAGE,'base64'));
    return { FaceMatches: [{Similarity: 94}, {Similarity: 98}] };
  });
  assert.deepEqual(await service(IMAGE,IMAGE), {similarity:98});
});
test('imagen inválida no llama AWS; fallo de permiso no significa no-match', async () => {
  let calls=0;
  const service=compareService(async () => { calls++; throw Object.assign(new Error('denied'), {name:'AccessDeniedException'}); });
  await assert.rejects(service('invalido',IMAGE));
  assert.equal(calls,0);
  await assert.rejects(service(IMAGE,IMAGE), /denied/);
});
test('fotos sin rostro se omiten sin inventar similitud', async () => {
  const service=compareService(async () => { throw Object.assign(new Error('no face'),{name:'InvalidParameterException'}); });
  assert.deepEqual(await service(IMAGE,IMAGE),{similarity:null});
});
test('no devuelve selfie de resultados en curso ni de referencia excesiva', async () => {
  const service=createResults(async () => ({Status:'IN_PROGRESS',ReferenceImage:{Bytes:Buffer.from([1])}}));
  assert.equal((await service(ID)).referenceBase64,null);
  const oversized=createResults(async () => ({Status:'SUCCEEDED',ReferenceImage:{Bytes:Buffer.alloc(1500001)}}));
  assert.equal((await oversized(ID)).referenceBase64,null);
});

test('propaga errores de AWS, no fabrica resultados', async () => {
  const send = async () => { throw new Error('AccessDenied'); };
  await assert.rejects(createSession(send)(ID), /AccessDenied/);
  await assert.rejects(createResults(send)(ID), /AccessDenied/);
});

test('rechaza estados y puntuaciones inválidas', async () => {
  for (const confidence of [NaN, Infinity, -1, 101, '99']) {
    await assert.rejects(createResults(async () => ({ Status: 'SUCCEEDED', Confidence: confidence }))(ID));
  }
  await assert.rejects(createResults(async () => ({ Status: 'PASSED' }))(ID));
});
