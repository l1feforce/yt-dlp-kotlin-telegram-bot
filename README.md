# yt-dlp-kotlin-telegram-bot

## About
`TODO()`

## Docker how-to instruction

1) Clone repo

2) Build image
   ```shell
   docker build -t tg-bot .
   ```

4) Run image
   ```shell
   docker run \
     -p 8080:8080 \
     -p 8081:8081 \
     -e BOT_TOKEN="<your-token>" \
     -e API_ID="<your-app-id>" \
     -e API_HASH="<your-api-hash>" \
     -e TELEGRAM_API_URL="http://127.0.0.1:8081/" \
     -e USERS="12312312,321312312" \
     -e COOKIES="~/path/to/cookies.txt" \
     tg-bot
   ```


## Manual how-to instruction

### Step 0 (optional)
If you want to use local telegram-bots-api to bypass the restrictions for uploading large files to telegram.

1) Go to https://tdlib.github.io/telegram-bot-api/build.html
2) Select your system parameters and get build script
4) Execute it to build binary
5) Run it
   > The only mandatory options are --api-id and --api-hash. You must obtain your own api_id and api_hash as described in https://core.telegram.org/api/obtaining_api_id and specify them using the --api-id and --api-hash options or the TELEGRAM_API_ID and TELEGRAM_API_HASH environment variables.

### Step 1
Install yt-dlp, ffmpeg and java, example for Ubuntu 24.04 is given below:

1) [yt-dlp install guide](https://github.com/yt-dlp/yt-dlp/wiki/Installation)
   On linux: `sudo apt install yt-dlp`
2) [ffmpeg install guide](https://ffmpeg.org/download.html)
   On linux: `sudo apt install ffmpeg`
3) java install
   On linux: `sudo apt install default-jdk`

### Step 2
Clone and run this repository

1) `git clone https://github.com/l1feforce/yt-dlp-kotlin-telegram-bot.git`
2) Add bot token, authorized users and override telegram api (if run it locally):
   ```shell
   export BOT_TOKEN="<YOUR_TOKEN>"
   export TELEGRAM_API_URL="http://127.0.0.1:8081/"
   export USERS="1231231231, 1414241414"
   ```

3) `./gradlew run`
