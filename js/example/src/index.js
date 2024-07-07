const app = require('./app');


const port = 8888;

app.listen(port, async () => {
  console.log(`Server is listening on port ${port}...`);
  
  const open = await import('open');
  open.default(`http://localhost:${port}`);
});