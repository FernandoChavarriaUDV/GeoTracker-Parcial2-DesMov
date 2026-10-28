const { RekognitionClient, GetFaceLivenessSessionResultsCommand } = require('@aws-sdk/client-rekognition');
const { requireUuid } = require('../../utils/validation');
let client;
const STATUSES = new Set(['CREATED', 'IN_PROGRESS', 'SUCCEEDED', 'FAILED', 'EXPIRED']);

function createService(send) {
  return async function getLivenessResults(sessionId) {
    const response = await send(new GetFaceLivenessSessionResultsCommand({
      SessionId: requireUuid(sessionId, 'sessionId')
    }));
    if (!STATUSES.has(response.Status)) throw new Error('Invalid provider status');
    const confidence = response.Confidence;
    if (confidence != null && (!Number.isFinite(confidence) || confidence < 0 || confidence > 100)) {
      throw new Error('Invalid provider confidence');
    }
    // Private backend invocation only. Selfie is transient; never log or return audit images/video.
    const bytes = response.Status === 'SUCCEEDED' ? response.ReferenceImage?.Bytes : undefined;
    const referenceBase64 = bytes?.length && bytes.length <= 1500000 ? Buffer.from(bytes).toString('base64') : null;
    return { status: response.Status, confidence: confidence ?? null, referenceBase64 };
  };
}

const getLivenessResults = createService(command => {
  client ??= new RekognitionClient({ region: process.env.AWS_REGION });
  return client.send(command);
});

module.exports = { getLivenessResults, createService };
