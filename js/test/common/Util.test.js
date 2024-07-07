import { Logger } from "../../src/common/Logger";

test('should return log tag with library name and provided class name', () => {
  const className = 'UtilTest';
  const traceId = 'test-vci-client';
  
  const logTag = Logger.getLogTag(className,traceId);
  
  expect(logTag).toBe('INJI-VCI-Client : UtilTest | traceID test-vci-client');
});
