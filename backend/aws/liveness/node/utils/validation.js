const UUID = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/;

function requireUuid(value, field) {
  if (typeof value !== 'string' || !UUID.test(value)) {
    throw new Error(`Invalid ${field}`);
  }
  return value;
}

module.exports = { requireUuid };
