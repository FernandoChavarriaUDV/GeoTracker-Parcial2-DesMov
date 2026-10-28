function imageBytes(value) {
  if (typeof value !== 'string' || value.length === 0 || value.length > 2000000 ||
      !/^[A-Za-z0-9+/]+={0,2}$/.test(value)) throw new Error('Invalid image');
  const bytes = Buffer.from(value, 'base64');
  if (bytes.toString('base64') !== value || bytes.length > 1500000) throw new Error('Invalid image');
  const jpeg = bytes.length > 3 && bytes[0] === 255 && bytes[1] === 216 && bytes[2] === 255;
  const png = bytes.length > 8 && bytes[0] === 137 && bytes[1] === 80 && bytes[2] === 78 && bytes[3] === 71;
  if (!jpeg && !png) throw new Error('JPEG or PNG required');
  return bytes;
}
module.exports = { imageBytes };
