const { RekognitionClient, CompareFacesCommand } = require('@aws-sdk/client-rekognition');
const { imageBytes } = require('../../utils/images');
let client;

function createService(send) {
  return async function compareFaces(referenceBase64, candidateBase64) {
    const command = new CompareFacesCommand({
      SourceImage: { Bytes: imageBytes(referenceBase64) },
      TargetImage: { Bytes: imageBytes(candidateBase64) },
      SimilarityThreshold: 0
    });
    let response;
    try { response = await send(command); }
    catch (error) {
      if (error.name === 'InvalidParameterException') return { similarity: null };
      throw error;
    }
    const scores = (response.FaceMatches ?? []).map(face => face.Similarity);
    if (scores.some(score => !Number.isFinite(score) || score < 0 || score > 100)) throw new Error('Invalid similarity');
    return { similarity: scores.length ? Math.max(...scores) : null };
  };
}
const compareFaces = createService(command => {
  client ??= new RekognitionClient({ region: process.env.AWS_REGION });
  return client.send(command);
});
module.exports = { createService, compareFaces };
