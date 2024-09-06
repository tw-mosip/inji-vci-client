const Logger =  require("../../src/common/Logger");

test('should set and return log tag with library name and provided class name', () => {
  
  Logger.setLogTag("AX123","UtilTest");
  
  const logTag = Logger.getLogTag();
  
  expect(logTag).toBe('INJI-VCI-Client: UtilTest | traceID: AX123 |');
});
