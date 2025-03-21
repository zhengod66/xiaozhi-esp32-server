from config.logger import setup_logging
import time
from core.utils.util import remove_punctuation_and_length
from core.handle.sendAudioHandle import send_stt_message
from core.handle.intentHandler import handle_user_intent

TAG = __name__
logger = setup_logging()


async def handleAudioMessage(conn, audio):
    if not conn.asr_server_receive:
        logger.bind(tag=TAG).debug(f"前期数据处理中，暂停接收")
        return
    if conn.client_listen_mode == "auto":
        have_voice = conn.vad.is_vad(conn, audio)
    else:
        have_voice = conn.client_have_voice

    # 如果本次没有声音，本段也没声音，就把声音丢弃了
    if have_voice == False and conn.client_have_voice == False:
        await no_voice_close_connect(conn)
        conn.asr_audio.clear()
        return
    conn.client_no_voice_last_time = 0.0
    conn.asr_audio.append(audio)
    # 如果本段有声音，且已经停止了
    if conn.client_voice_stop:
        conn.client_abort = False
        conn.asr_server_receive = False
        # 音频太短了，无法识别
        if len(conn.asr_audio) < 3:
            conn.asr_server_receive = True
        else:
            text, file_path = await conn.asr.speech_to_text(conn.asr_audio, conn.session_id)
            logger.bind(tag=TAG).info(f"识别文本: {text}")
            text_len, _ = remove_punctuation_and_length(text)
            if text_len > 0:
                await startToChat(conn, text)
            else:
                conn.asr_server_receive = True
        conn.asr_audio.clear()
        conn.reset_vad_states()


async def startToChat(conn, text):
    # 首先进行意图分析
    intent_handled = await handle_user_intent(conn, text)
    
    if intent_handled:
        # 如果意图已被处理，不再进行聊天
        conn.asr_server_receive = True
        return
    
    # 检查LLM是否被禁用
    if conn.config.get("disable_llm", False):
        logger.bind(tag=TAG).warning("LLM已被禁用，无法处理聊天。发送默认回复。")
        await send_stt_message(conn, text)
        # 发送一个默认响应，表示系统无法处理聊天
        default_response = "对不起，我当前无法处理您的请求。语言模型服务未启用。"
        # 直接通过TTS功能播放默认响应
        if hasattr(conn, 'speak_and_play') and callable(conn.speak_and_play):
            future = conn.executor.submit(conn.speak_and_play, default_response)
            conn.tts_queue.put(future)
        conn.asr_server_receive = True
        return
    
    # 意图未被处理，继续常规聊天流程
    await send_stt_message(conn, text)
    if conn.use_function_call_mode:
        # 使用支持function calling的聊天方法
        conn.executor.submit(conn.chat_with_function_calling, text)
    else:
        conn.executor.submit(conn.chat, text)


async def no_voice_close_connect(conn):
    if conn.client_no_voice_last_time == 0.0:
        conn.client_no_voice_last_time = time.time() * 1000
    else:
        no_voice_time = time.time() * 1000 - conn.client_no_voice_last_time
        close_connection_no_voice_time = conn.config.get("close_connection_no_voice_time", 120)
        if no_voice_time > 1000 * close_connection_no_voice_time:
            conn.client_abort = False
            conn.asr_server_receive = False
            prompt = "时间过得真快，我都好久没说话了。请你用十个字左右话跟我告别，以'再见'或'拜拜'为结尾"
            await startToChat(conn, prompt)
