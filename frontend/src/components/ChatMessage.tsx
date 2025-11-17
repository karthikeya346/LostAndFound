import React from 'react';
import { Avatar, AvatarFallback, AvatarImage } from './ui/avatar';
import { ChatMessage as ChatMessageType } from '../types';

interface ChatMessageProps {
  message: ChatMessageType;
  currentUserId: number;
}

export const ChatMessage: React.FC<ChatMessageProps> = ({ message, currentUserId }) => {
  const isOwnMessage = message.senderId === currentUserId;

  return (
    <div className={`flex gap-3 ${isOwnMessage ? 'flex-row-reverse' : 'flex-row'}`}>
      <Avatar className="h-8 w-8">
        <AvatarImage src={`https://avatar.vercel.sh/${message.sender?.username}`} />
        <AvatarFallback>{message.sender?.username.charAt(0).toUpperCase()}</AvatarFallback>
      </Avatar>
      <div className={`flex-1 space-y-1 ${isOwnMessage ? 'text-right' : ''}`}>
        <div className="flex items-baseline gap-2">
          <span className="text-sm font-semibold">{message.sender?.username || 'Unknown'}</span>
          <span className="text-xs text-muted-foreground">
            {new Date(message.sentAt).toLocaleTimeString()}
          </span>
        </div>
        <div
          className={`inline-block rounded-lg px-4 py-2 ${
            isOwnMessage
              ? 'bg-electric text-white rounded-tr-none'
              : 'bg-muted rounded-tl-none'
          }`}
        >
          {message.message}
        </div>
      </div>
    </div>
  );
};

