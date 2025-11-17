import React from 'react';
import { FileText, Clock, CheckCircle, XCircle, AlertCircle } from 'lucide-react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from './ui/card';
import { Badge } from './ui/badge';
import { Button } from './ui/button';
import { Claim } from '../types';

interface ClaimCardProps {
  claim: Claim;
  onApprove?: (claimId: number) => void;
  onReject?: (claimId: number) => void;
  showActions?: boolean;
}

export const ClaimCard: React.FC<ClaimCardProps> = ({ claim, onApprove, onReject, showActions = false }) => {
  const getStatusIcon = () => {
    switch (claim.status) {
      case 'PENDING':
        return <Clock className="h-4 w-4" />;
      case 'APPROVED':
        return <CheckCircle className="h-4 w-4 text-green-500" />;
      case 'REJECTED':
        return <XCircle className="h-4 w-4 text-red-500" />;
      case 'CANCELLED':
        return <AlertCircle className="h-4 w-4 text-yellow-500" />;
      default:
        return <FileText className="h-4 w-4" />;
    }
  };

  const getStatusColor = () => {
    switch (claim.status) {
      case 'PENDING':
        return 'bg-yellow-500';
      case 'APPROVED':
        return 'bg-green-500';
      case 'REJECTED':
        return 'bg-red-500';
      case 'CANCELLED':
        return 'bg-gray-500';
      default:
        return 'bg-blue-500';
    }
  };

  return (
    <Card className="hover:shadow-lg transition-shadow">
      <CardHeader>
        <div className="flex items-start justify-between">
          <CardTitle className="text-xl">Claim #{claim.id}</CardTitle>
          <Badge className={getStatusColor()}>
            {claim.status}
          </Badge>
        </div>
        <CardDescription>{claim.description}</CardDescription>
      </CardHeader>

      <CardContent>
        <div className="space-y-2 text-sm">
          <div className="flex items-center gap-2 text-muted-foreground">
            {getStatusIcon()}
            <span>Status: {claim.status}</span>
          </div>
          <div className="flex items-center gap-2 text-muted-foreground">
            <Clock className="h-4 w-4" />
            <span>{new Date(claim.claimDate).toLocaleString()}</span>
          </div>
        </div>
      </CardContent>

      {showActions && claim.status === 'PENDING' && (
        <div className="p-4 border-t flex gap-2">
          <Button onClick={() => onApprove?.(claim.id)} className="flex-1 bg-green-500 hover:bg-green-600">
            Approve
          </Button>
          <Button onClick={() => onReject?.(claim.id)} variant="destructive" className="flex-1">
            Reject
          </Button>
        </div>
      )}
    </Card>
  );
};


