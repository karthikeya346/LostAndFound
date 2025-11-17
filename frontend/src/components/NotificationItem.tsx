import React from 'react';
import { Bell, Circle } from 'lucide-react';
import { Card } from './ui/card';
import { Badge } from './ui/badge';
import { Notification } from '../types';

interface NotificationItemProps {
  notification: Notification;
  onMarkAsRead?: (id: number) => void;
  onDelete?: (id: number) => void;
}

export const NotificationItem: React.FC<NotificationItemProps> = ({
  notification,
  onMarkAsRead,
  onDelete,
}) => {
  const getTypeColor = () => {
    switch (notification.type) {
      case 'CLAIM':
        return 'bg-blue-500';
      case 'CHAT':
        return 'bg-green-500';
      case 'OTP':
        return 'bg-amber-500';
      default:
        return 'bg-gray-500';
    }
  };

  return (
    <Card className={`hover:shadow-lg transition-shadow ${!notification.isRead ? 'border-l-4 border-l-electric' : ''}`}>
      <div className="p-4">
        <div className="flex items-start justify-between gap-4">
          <div className="flex gap-3 flex-1">
            <div className={`mt-1 ${getTypeColor()} rounded-full p-2`}>
              <Bell className="h-4 w-4 text-white" />
            </div>
            <div className="flex-1 min-w-0">
              <p className="font-semibold text-sm">{notification.title}</p>
              <p className="text-sm text-muted-foreground mt-1">{notification.message}</p>
              <p className="text-xs text-muted-foreground mt-2">
                {new Date(notification.createdAt).toLocaleString()}
              </p>
            </div>
          </div>
          <div className="flex gap-2">
            {!notification.isRead && (
              <Circle className="h-2 w-2 fill-electric text-electric" />
            )}
            <Badge variant="outline">{notification.type}</Badge>
          </div>
        </div>
      </div>
    </Card>
  );
};

