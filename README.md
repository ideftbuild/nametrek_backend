# Nametrek Backend 🚀

## Project Description

Growing up, I loved playing this word game—it was fun and challenging. Recently, I felt nostalgic and wanted to bring it back. So, I decided to recreate it on the web, mixing old memories with new possibilities. 

Nametrek is a fun and fast-paced game where players race against the clock. The challenge? Name items in a chosen category—countries, animals, cars, or even everyday objects—before time runs out. 

But don't blink! If you hesitate too long or give a wrong answer, you're out! The game keeps going until one quick thinker is crowned the winner. 

It's not just a game—it's a challenge. It pushes you to think quickly, type faster, and stay sharp under pressure. Perfect for friends, families, or anyone who loves a lighthearted yet thrilling way to connect online.

## 📋 Table of Contents

- [Project Description](#project-description)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Getting Started](#getting-started)
- [API Endpoints](#api-endpoints)
- [Contributing](#contributing)
- [License](#license)

## 🛠 Prerequisites

Before you begin, ensure you have the following:

- Git
- Appropriate development environment (specific details to be provided upon request)
- Access to required environment variables

## 🔧 Installation

1. Clone the repository:
```bash
git clone https://github.com/ideftbuild/nametrek_backend.git
cd nametrek_backend
./gradlew build
```

2. Contact project maintainer for environment configuration details

3) Initialize development server
```
./start_development.sh
```

## 🚀 Getting Started
After installation, the development server will be running locally. Ensure all environment variables are correctly configured.

## 🌐 API Endpoints
| Endpoint                                | Method | Description                         |
|-----------------------------------------|--------|-------------------------------------|
| `/games/{roomId}`                       | GET    | Start a new game                   |
| `/games/{roomId}/{answer}`              | GET    | Send answer                        |
| `/rooms`                                | POST   | Create a room                      |
| `/rooms/{roomId}/join`                  | PUT    | Join a room by ID                  |
| `/rooms/join`                           | PUT    | Join a room by code                |
| `/rooms/{roomId}/players/me`            | GET    | Get current player in a room       |
| `/rooms/{roomId}/missed-updates`        | GET    | Get room missed updates            |
| `/contact`                              |        |                                     |

## 🤝 Contributing

We welcome contributions! Here's how you can help:

1. Fork the repository

2. Create a feature branch:

```bash
git checkout -b feature/your-feature-name
```

3. Commit your changes:
```bash
git commit -am 'Add new feature'
```

4. Push to your branch:
```bash
git push origin feature/your-feature-name
```

5. Open a pull request

## 📄 License
MIT License
Copyright (c) 2025 Akaniyene Effiong
Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
