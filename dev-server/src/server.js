const express = require('express');
const fs = require('fs');
const path = require('path');

const app = express();
const PORT = process.env.PORT || 8080;

app.use('/logos', express.static(path.join(__dirname, 'data/logos')));

// M3U Playlist
app.get('/playlist.m3u', (req, res) => {
  res.set('Content-Type', 'audio/x-mpegurl');
  res.send(fs.readFileSync(path.join(__dirname, 'data/sample.m3u'), 'utf8'));
});

// Xtream Codes API
app.get('/player_api.php', (req, res) => {
  const { username, password, action } = req.query;

  if (!username || !password) {
    return res.json({ user_info: { auth: false, message: 'Missing credentials' } });
  }
  if (username !== 'demo' || password !== 'demo') {
    return res.json({ user_info: { auth: false, message: 'Invalid credentials' } });
  }

  switch (action) {
    case 'get_live_categories':
      return res.json([
        { category_id: '1', category_name: 'Entertainment' },
        { category_id: '2', category_name: 'Sports' },
        { category_id: '3', category_name: 'News' },
        { category_id: '4', category_name: 'Movies' },
      ]);

    case 'get_live_streams': {
      const streams = JSON.parse(
        fs.readFileSync(path.join(__dirname, 'data/xtream-streams.json'), 'utf8')
      );
      const cat = req.query.category_id;
      return res.json(cat ? streams.filter(s => s.category_id === cat) : streams);
    }

    default:
      return res.json({
        user_info: {
          auth: true, username: 'demo', status: 'Active',
          exp_date: '1893456000', is_trial: '0', max_connections: '1',
        },
        server_info: {
          url: `http://localhost:${PORT}`, port: PORT.toString(),
          https_port: '', server_protocol: 'http', rtmp_port: '',
        },
      });
  }
});

// XMLTV EPG
app.get('/epg.xml', (req, res) => {
  res.set('Content-Type', 'text/xml');
  res.sendFile(path.join(__dirname, 'data/sample.xml'));
});

app.listen(PORT, () => {
  console.log(`🌐 Mock IPTV server → http://localhost:${PORT}`);
  console.log(`   M3U:    http://localhost:${PORT}/playlist.m3u`);
  console.log(`   Xtream: http://localhost:${PORT}/player_api.php?username=demo&password=demo`);
  console.log(`   EPG:    http://localhost:${PORT}/epg.xml`);
});
