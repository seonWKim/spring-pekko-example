import React, {useEffect, useState} from 'react';
import SockJS from 'sockjs-client';
import {Client} from '@stomp/stompjs';
import 'bootstrap/dist/css/bootstrap.min.css';

const Chat = () => {
  const [stompClient, setStompClient] = useState(null);
  const [roomId, setRoomId] = useState(null);
  const [from, setFrom] = useState('');
  const [text, setText] = useState('');
  const [messages, setMessages] = useState([]);
  const [port, setPort] = useState('8080');
  const [subscription, setSubscription] = useState(null);

  useEffect(() => {
    if (port) {
      const socket = new SockJS(`http://localhost:${port}/ws`);
      const client = new Client({
        webSocketFactory: () => socket,
        onConnect: (frame) => {
          console.log('Connected: ' + frame);
          setStompClient(client);
        },
      });
      client.activate();
    }
  }, [port]);

  const joinRoom = (room) => {
    if (!from) {
      alert('From should be set!!');
      return;
    }

    // Unsubscribe from the previous room if subscribed
    if (subscription) {
      subscription.unsubscribe();
    }

    setRoomId(room);

    // Subscribe to the new room and store the subscription reference
    const newSubscription = stompClient.subscribe(`/topic/${room}`, (message) => {
      const messageBody = JSON.parse(message.body)
                               .map(line => `• ${line}`)
                               .join('\n');
      console.log(`Received message ${messageBody}`);
      showMessage(messageBody);
    });

    setSubscription(newSubscription); // Store the new subscription

    stompClient.publish({
      destination: `/app/chat/join`,
      body: JSON.stringify({
        roomId: room,
        user: from
      })
    });
  };

  const sendMessage = () => {
    const message = {
      roomId: roomId,
      user: from,
      message: text
    };
    console.log(`sending message to room(${roomId}) with message ${JSON.stringify(message)}`);

    fetch(`http://localhost:${port}/chat/send`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(message)
    }).then(_ => {
      console.log('Message sent');
    }).catch((error) => {
      console.error('Error:', error);
    });
  };

  const showMessage = (message) => {
    setMessages((prevMessages) => [...prevMessages, message]);
  };

  const clearMessages = () => {
    setMessages([]);
  };

  return (
    <div className="container mt-5">
      <div className="row">
        <div className="col-md-6 offset-md-3">
          <h2 className="text-center">Chat Application</h2>
          <div className="text-center mb-3">
            <p>Current Port: {port}</p>
            <p>Current Room: {roomId ? roomId : 'None'}</p>
          </div>
          <div className="form-group text-center mb-3">
            <button className="btn btn-primary m-3" onClick={() => setPort('8080')}>
              Port 8080
            </button>
            <button className="btn btn-primary m-3" onClick={() => setPort('8081')}>
              Port 8081
            </button>
            <button className="btn btn-primary m-3" onClick={() => setPort('8082')}>
              Port 8082
            </button>
          </div>
          <div className="form-group text-center mb-3">
            <button className="btn btn-success m-3" onClick={() => joinRoom('1')}>
              Join Room 1
            </button>
            <button className="btn btn-success m-3" onClick={() => joinRoom('2')}>
              Join Room 2
            </button>
            <button className="btn btn-success m-3" onClick={() => joinRoom('3')}>
              Join Room 3
            </button>
          </div>
          <div id="response" className="border rounded p-3" style={{height: '300px', overflowY: 'scroll', whiteSpace: 'pre-wrap', textAlign: 'left'}}>
            {messages.map((message, index) => (
              <p key={index} style={{border: '1px solid lightgray', padding: '5px', borderRadius: '5px', marginBottom: '5px'}}>
                {message}
              </p>
            ))}
          </div>
          <div className="form-group mt-3 mb-3">
            <input
              type="text"
              className="form-control"
              placeholder="Your name"
              value={from}
              onChange={(e) => setFrom(e.target.value)}
            />
          </div>
          <div className="form-group">
            <input
              type="text"
              className="form-control"
              placeholder="Your message"
              value={text}
              onChange={(e) => setText(e.target.value)}
            />
          </div>
          <div className="form-group text-center">
            <button className="btn btn-primary mt-3" onClick={sendMessage}>
              Send Message
            </button>
          </div>
          <div className="form-group text-center">
            <button className="btn btn-danger mt-3" onClick={clearMessages}>
              Clear Messages
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Chat;
