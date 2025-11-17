import React from 'react';
import { Package, MapPin, Calendar, User, Image as ImageIcon } from 'lucide-react';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from './ui/card';
import { Badge } from './ui/badge';
import { Button } from './ui/button';
import { Item } from '../types';
import { Avatar, AvatarFallback, AvatarImage } from './ui/avatar';

interface ItemCardProps {
  item: Item;
  onClaim?: (itemId: number) => void;
  showActions?: boolean;
}

export const ItemCard: React.FC<ItemCardProps> = ({ item, onClaim, showActions = true }) => {
  const isLost = item.type === 'LOST';
  const isApproved = item.status === 'APPROVED';

  return (
    <Card className="hover:shadow-lg transition-shadow">
      {item.imagePath && (
        <div className="relative h-48 overflow-hidden rounded-t-lg bg-muted">
          <img
            src={item.imagePath}
            alt={item.title}
            className="w-full h-full object-cover"
            onError={(e) => {
              const target = e.target as HTMLImageElement;
              target.style.display = 'none';
            }}
          />
          <div className="absolute inset-0 flex items-center justify-center bg-muted">
            <ImageIcon className="h-12 w-12 text-muted-foreground" />
          </div>
        </div>
      )}
      
      <CardHeader>
        <div className="flex items-start justify-between">
          <CardTitle className="text-xl">{item.title}</CardTitle>
          <Badge
            variant={isLost ? 'destructive' : 'default'}
            className={isLost ? 'bg-red-500' : 'bg-green-500'}
          >
            {item.type}
          </Badge>
        </div>
        <CardDescription className="line-clamp-2">{item.description}</CardDescription>
      </CardHeader>

      <CardContent>
        <div className="space-y-2 text-sm">
          <div className="flex items-center gap-2 text-muted-foreground">
            <MapPin className="h-4 w-4" />
            <span>{item.location}</span>
          </div>
          <div className="flex items-center gap-2 text-muted-foreground">
            <Calendar className="h-4 w-4" />
            <span>{new Date(item.dateReported).toLocaleDateString()}</span>
          </div>
          {item.reportedBy && (
            <div className="flex items-center gap-2 text-muted-foreground">
              <User className="h-4 w-4" />
              <span>{item.reportedBy}</span>
            </div>
          )}
          <Badge variant={isApproved ? 'default' : 'secondary'}>{item.status}</Badge>
        </div>
      </CardContent>

      {showActions && onClaim && (
        <CardFooter>
          <Button
            onClick={() => onClaim(item.id)}
            disabled={!isApproved || item.type === 'LOST'}
            className="w-full"
          >
            {isLost ? 'View Details' : 'Claim This Item'}
          </Button>
        </CardFooter>
      )}
    </Card>
  );
};


