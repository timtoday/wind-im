/** 
 * Copyright 2018-2028 Akaxin Group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package com.windchat.im.message.user2.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.windchat.common.command.Command;
import com.windchat.common.logs.LogUtils;
import com.akaxin.proto.core.CoreProto;
import com.akaxin.proto.site.ImCtsMessageProto;
import com.windchat.im.storage.api.IMessageDao;
import com.windchat.im.storage.bean.U2MessageBean;
import com.windchat.im.storage.service.MessageDaoService;

/**
 * 二人图片消息处理
 * 
 * @author Sam{@link an.guoyue254@gmail.com}
 * @since 2018-04-26 14:56:25
 */
public class U2MessageImageHandler extends AbstractU2Handler<Command> {
	private static final Logger logger = LoggerFactory.getLogger(U2MessageImageHandler.class);
	private IMessageDao messageDao = new MessageDaoService();

	public Boolean handle(Command command) {
		try {
			int type = command.getMsgType();
			// 二人图片消息类型
			if (CoreProto.MsgType.IMAGE_VALUE == type) {
				ImCtsMessageProto.ImCtsMessageRequest request = ImCtsMessageProto.ImCtsMessageRequest
						.parseFrom(command.getParams());
				String siteUserId = command.getSiteUserId();
				String proxySiteUserId = request.getImage().getSiteUserId();
				String siteFriendId = request.getImage().getSiteFriendId();
				String msgId = request.getImage().getMsgId();
				String imageId = request.getImage().getImageId();

				U2MessageBean u2Bean = new U2MessageBean();
				u2Bean.setMsgId(msgId);
				u2Bean.setMsgType(type);
				u2Bean.setSiteUserId(siteFriendId);
				u2Bean.setSendUserId(command.isProxy() ? proxySiteUserId : siteUserId);
				u2Bean.setReceiveUserId(siteFriendId);
				u2Bean.setContent(imageId);
				long msgTime = System.currentTimeMillis();
				u2Bean.setMsgTime(msgTime);

				LogUtils.requestDebugLog(logger, command, u2Bean.toString());

				boolean success = messageDao.saveU2Message(u2Bean);

				if (success && command.isProxy()) {
					U2MessageBean proxyBean = new U2MessageBean();
					proxyBean.setMsgId(buildU2MsgId(proxySiteUserId));
					proxyBean.setMsgType(type);
					proxyBean.setSiteUserId(proxySiteUserId);
					proxyBean.setSendUserId(proxySiteUserId);
					proxyBean.setReceiveUserId(siteFriendId);
					proxyBean.setContent(imageId);
					proxyBean.setMsgTime(msgTime);
					messageDao.saveU2Message(proxyBean);
				}

				msgStatusResponse(command, msgId, msgTime, success);

				return success;
			}

			return true;
		} catch (Exception e) {
			LogUtils.requestErrorLog(logger, command, this.getClass(), e);
		}

		return false;
	}

}
