const { createLivenessSession } = require('./services/rekognition');

// Invocación privada y firmada desde Spring; no es una Function URL pública.
exports.handler = async event => createLivenessSession(event?.requestId);
