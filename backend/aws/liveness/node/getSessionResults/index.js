const { getLivenessResults } = require('./services/rekognition');

// El backend verifica primero el JWT y que la sesión pertenece al usuario.
exports.handler = async event => getLivenessResults(event?.sessionId);
