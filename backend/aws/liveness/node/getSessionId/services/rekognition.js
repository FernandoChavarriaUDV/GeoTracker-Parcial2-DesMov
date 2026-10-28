const { RekognitionClient, CreateFaceLivenessSessionCommand } = require('@aws-sdk/client-rekognition');
const { requireUuid } = require('../../utils/validation');
let client;

function createService(send) {
  return async function createLivenessSession(requestId) {
    const token = requireUuid(requestId, 'requestId');
    const response = await send(new CreateFaceLivenessSessionCommand({
      ClientRequestToken: token,
      Settings: { AuditImagesLimit: 0 }
    }));
    return { sessionId: requireUuid(response.SessionId, 'sessionId') };
  };
}

const createLivenessSession = createService(command => {
  client ??= new RekognitionClient({ region: process.env.AWS_REGION });
  return client.send(command);
});

module.exports = { createLivenessSession, createService };
