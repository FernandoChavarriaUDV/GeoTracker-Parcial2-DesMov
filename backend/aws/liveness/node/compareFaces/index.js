const { compareFaces } = require('./services/rekognition');
exports.handler = async event => compareFaces(event?.referenceBase64, event?.candidateBase64);
