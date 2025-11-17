import React, { useEffect, useRef, useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { usePreviewMode } from '../contexts/PreviewModeContext';
import { useTheme } from '../contexts/ThemeContext';
import { Button } from '../components/ui/button';
import AdminChatViewer from '../components/AdminChatViewer';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../components/ui/card';
import { 
  Users, 
  Package, 
  FileText, 
  MessageCircle, 
  Bell, 
  Shield, 
  BarChart3,
  Moon, 
  Sun,
  Menu,
  X,
  Settings as SettingsIcon
} from 'lucide-react';
import analytics from '../services/analytics';
import auditService from '../services/audit';
import adminAnalytics from '../services/adminAnalytics';
import usersService, { User } from '../services/users';
import itemsService, { Item } from '../services/items';
import claimsService, { Claim } from '../services/claims';
import chatsService, { ChatSession, ChatMessage, ChatParticipant } from '../services/chats';
import notificationsService, { Notification as AppNotification } from '../services/notifications';
import api, { fileUrl } from '../services/api';

// Placeholder components for different sections
const UsersPage = ({ users, loading, error, onBan, onUnban }: { users: User[]; loading: boolean; error: string | null; onBan: (id: number) => void; onUnban: (id: number) => void }) => (
  <div className="space-y-6">
    {/* removed manage users tag */}
    <Card className="bg-white/40 dark:bg-slate-900/50 backdrop-blur-sm border border-white/30 dark:border-slate-700/40">
      <CardHeader>
        <CardTitle>User Management</CardTitle>
        <CardDescription>
          View and manage all registered users
        </CardDescription>
      </CardHeader>
      <CardContent>
        {error ? (
          <div className="text-center py-8 text-muted-foreground">{error}</div>
        ) : loading ? (
          <div className="text-center py-8 text-muted-foreground">Loading...</div>
        ) : users.length === 0 ? (
          <div className="text-center py-8 text-muted-foreground">
            <Users className="mx-auto h-12 w-12 mb-4" />
            <p>No users found. Users will appear here once they register!</p>
          </div>
        ) : (
          <div className="space-y-2">
            <div className="text-sm text-muted-foreground">Total: {users.length}</div>
            <ul className="divide-y">
              {users.slice(0, 8).map(u => (
                <li key={u.id} className="py-2 flex items-center justify-between">
                  <div>
                    <div className="font-medium flex items-center gap-2">
                      <span>{u.username}</span>
                      {u.status === 'DISABLED' && (
                        <span className="px-2 py-0.5 text-[10px] rounded-full bg-red-500/15 text-red-600 dark:text-red-300 border border-red-500/30">BANNED</span>
                      )}
                    </div>
                    <div className="text-xs text-muted-foreground">{u.email}</div>
                  </div>
                  <div className="flex items-center gap-2">
                    <div className="text-xs text-muted-foreground mr-2">{u.role}</div>
                    {u.role !== 'ADMIN' && (
                      <>
                        <Button size="sm" variant="destructive" disabled={u.status === 'DISABLED'} onClick={() => onBan(u.id)}>Ban</Button>
                        <Button size="sm" variant="default" disabled={u.status !== 'DISABLED'} onClick={() => onUnban(u.id)}>Unban</Button>
                      </>
                    )}
                  </div>
                </li>
              ))}
            </ul>
          </div>
        )}
      </CardContent>
    </Card>
  </div>
);

const ItemsPage = ({ items, loading, error, onOpenImage, onOpenDetails, onApprove, onReject }: { items: Item[]; loading: boolean; error: string | null; onOpenImage: (url: string) => void; onOpenDetails: (item: Item) => void; onApprove: (id: number) => void; onReject: (id: number) => void }) => {
  const [q, setQ] = useState("");
  const filtered = React.useMemo(() => {
    const query = q.trim().toLowerCase();
    if (!query) return items;
    return items.filter((it: any) => {
      const fields = [it.title, it.description, it.location, it.type, it.status, (it as any).username]
        .filter(Boolean)
        .map((s: any) => String(s).toLowerCase());
      return fields.some((f: string) => f.includes(query));
    });
  }, [q, items]);

  return (
    <div className="space-y-6">
      <Card className="bg-white/40 dark:bg-slate-900/50 backdrop-blur-sm border border-white/30 dark:border-slate-700/40">
        <CardHeader>
          <CardTitle>All Items</CardTitle>
          <CardDescription>
            Review and moderate all reported items
          </CardDescription>
        </CardHeader>
        <CardContent>
          {error ? (
            <div className="text-center py-8 text-muted-foreground">{error}</div>
          ) : loading ? (
            <div className="text-center py-8 text-muted-foreground">Loading...</div>
          ) : items.length === 0 ? (
            <div className="text-center py-8 text-muted-foreground">
              <Package className="mx-auto h-12 w-12 mb-4" />
              <p>No items to review. Items will appear here once users report them!</p>
            </div>
          ) : (
            <div className="space-y-3">
              <div className="flex items-center justify-between gap-3">
                <div className="text-sm text-muted-foreground">Showing {filtered.length} of {items.length}</div>
                <input
                  placeholder="Search items..."
                  value={q}
                  onChange={(e) => setQ(e.target.value)}
                  className="w-64 max-w-[50%] px-3 py-2 rounded border bg-transparent"
                />
              </div>
              <ul className="divide-y">
                {filtered.slice(0, 50).map((it: Item) => {
                  const status = ((it.status as any) || '').toString().toUpperCase();
                  const isReturned = status === 'RETURNED';
                  return (
                    <li key={it.id} className={`py-3 flex items-center justify-between gap-4 ${isReturned ? 'opacity-70' : ''}`}>
                      <div className="flex items-center gap-3">
                        {(() => { const img = (it as any).image_path || (it as any).imagePath; return img ? (
                          <img
                            src={fileUrl(img)}
                            alt={it.title}
                            className="w-16 h-16 rounded object-cover cursor-pointer"
                            onClick={() => onOpenImage(fileUrl(img))}
                          />
                        ) : (
                          <div className="w-16 h-16 rounded bg-slate-200 dark:bg-slate-800 flex items-center justify-center text-xs text-muted-foreground">No Image</div>
                        ); })()}
                        <div className="relative">
                          <div className="font-medium flex items-center gap-2">
                            <span>{it.title}</span>
                            {isReturned && (
                              <span className="px-2 py-0.5 text-[10px] rounded-full bg-green-500/15 text-green-700 dark:text-green-300 border border-green-500/30">RETURNED</span>
                            )}
                          </div>
                          <div className="text-xs text-muted-foreground">{it.location} • {status}</div>
                          {isReturned && (
                            <span className="absolute -top-3 -left-2 rotate-[-10deg] bg-green-600 text-white text-[10px] px-2 py-0.5 rounded shadow">RETURNED</span>
                          )}
                        </div>
                      </div>
                      <div className="flex items-center gap-2">
                        <div className="text-xs text-muted-foreground hidden sm:block">{it.type}</div>
                        {status === 'UNDER' && !isReturned && (
                          <>
                            <Button size="sm" variant="default" onClick={() => onApprove(it.id)}>Approve</Button>
                            <Button size="sm" variant="destructive" onClick={() => onReject(it.id)}>Reject</Button>
                          </>
                        )}
                        <Button size="sm" variant="outline" onClick={() => onOpenDetails(it)}>View Details</Button>
                      </div>
                    </li>
                  );
                })}
              </ul>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
};
const ClaimsPage = ({ claims, loading, error, onStartChat }: { claims: Claim[]; loading: boolean; error: string | null; onStartChat: (claim: Claim) => void }) => (
  <div className="space-y-6">
    <Card className="bg-white/40 dark:bg-slate-900/50 backdrop-blur-sm border border-white/30 dark:border-slate-700/40">
      <CardHeader>
        <CardTitle>Claim Management</CardTitle>
        <CardDescription>
          Review and approve/reject user claims
        </CardDescription>
      </CardHeader>
      <CardContent>
        {error ? (
          <div className="text-center py-8 text-muted-foreground">{error}</div>
        ) : loading ? (
          <div className="text-center py-8 text-muted-foreground">Loading...</div>
        ) : claims.length === 0 ? (
          <div className="text-center py-8 text-muted-foreground">
            <FileText className="mx-auto h-12 w-12 mb-4" />
            <p>No claims to review. Claims will appear here once users submit them!</p>
          </div>
        ) : (
          <div className="space-y-2">
            <div className="text-sm text-muted-foreground">Pending: {claims.length}</div>
            <ul className="divide-y">
              {claims.slice(0, 8).map(c => (
                <li key={c.id} className="py-2 flex items-center justify-between">
                  <div>
                    <div className="font-medium">Claim #{c.id}</div>
                    <div className="text-xs text-muted-foreground">Item {c.item_id} • User {c.user_id} • {c.status}</div>
                  </div>
                  <div>
                    <Button size="sm" variant="outline" onClick={() => onStartChat(c)}>Start Chat</Button>
                  </div>
                </li>
              ))}
            </ul>
          </div>
        )}
      </CardContent>
    </Card>
  </div>
);

const ChatsPage = ({ sessions, loading, error, onClose, onDelete, onView }: { sessions: ChatSession[]; loading: boolean; error: string | null; onClose: (chatId: number) => void; onDelete: (chatId: number) => void; onView: (chatId: number) => void }) => {
  const handleOnClose = async (chatId: number) => {
    await onClose(chatId);
    // reload chats list after closing
    // assuming you have a function to reload chats list
    // reloadChatsList();
  };

  

  return (
    <div className="space-y-6">
      <Card className="bg-white/35 dark:bg-slate-900/50 backdrop-blur-sm border border-white/30 dark:border-slate-700/40">
        <CardHeader>
          <CardTitle>Chat Moderation</CardTitle>
          <CardDescription>
            Monitor and moderate chat sessions
          </CardDescription>
        </CardHeader>
        <CardContent>
          {error ? (
            <div className="text-center py-8 text-muted-foreground">{error}</div>
          ) : loading ? (
            <div className="text-center py-8 text-muted-foreground">Loading...</div>
          ) : sessions.length === 0 ? (
            <div className="text-center py-8 text-muted-foreground">
              <MessageCircle className="mx-auto h-12 w-12 mb-4" />
              <p>No active chats. Chat sessions will appear here once claims are approved!</p>
            </div>
          ) : (
            <div className="space-y-2">
              <div className="text-sm text-muted-foreground">Total: {sessions.length}</div>
              <ul className="divide-y">
                {sessions.map(s => (
                  <li key={s.id} className="py-2 flex items-center justify-between">
                    <div>
                      <div className="font-medium">Chat #{s.id}</div>
                      <div className="text-xs text-muted-foreground">Item {s.itemId} • Claim {s.claimId} • {s.status}</div>
                    </div>
                    <div className="flex items-center gap-2">
                      <Button size="sm" variant="outline" onClick={() => onView(s.id)}>View</Button>
                      {s.status !== 'CLOSED' ? (
                        <Button size="sm" variant="destructive" onClick={() => handleOnClose(s.id)}>Close</Button>
                      ) : (
                        <Button size="sm" variant="destructive" onClick={() => onDelete(s.id)}>Delete</Button>
                      )}
                    </div>
                  </li>
                ))}
              </ul>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
};

const NotificationsPage = ({ notifications, loading, error, onDelete }: { notifications: AppNotification[]; loading: boolean; error: string | null; onDelete: (id: number, userId?: number) => void }) => (
  <div className="space-y-6">
    <Card className="bg-white/35 dark:bg-slate-900/50 backdrop-blur-sm border border-white/30 dark:border-slate-700/40">
      <CardHeader>
        <CardTitle>Notification Management</CardTitle>
        <CardDescription>
          Send broadcasts and manage notifications
        </CardDescription>
      </CardHeader>
      <CardContent>
        {error ? (
          <div className="text-center py-8 text-muted-foreground">{error}</div>
        ) : loading ? (
          <div className="text-center py-8 text-muted-foreground">Loading...</div>
        ) : notifications.length === 0 ? (
          <div className="text-center py-8 text-muted-foreground">
            <Bell className="mx-auto h-12 w-12 mb-4" />
            <p>No notifications to manage.</p>
          </div>
        ) : (
          <div className="space-y-2">
            <div className="text-sm text-muted-foreground">Total: {notifications.length}</div>
            <ul className="divide-y">
              {notifications.slice(0, 20).map(n => (
                <li key={n.id} className="py-2 flex items-center justify-between">
                  <div>
                    <div className="font-medium text-sm">{n.type ?? 'INFO'}</div>
                    <div className="text-xs text-muted-foreground">{n.message}</div>
                    <div className="text-[10px] text-muted-foreground mt-1">User {n.user_id}</div>
                  </div>
                  <div className="flex items-center gap-2">
                    <Button size="sm" variant="destructive" onClick={() => onDelete(n.id, n.user_id)}>Delete</Button>
                  </div>
                </li>
              ))}
            </ul>
          </div>
        )}
      </CardContent>
    </Card>
  </div>
);

const AuditLogsPage = () => {
  const [logs, setLogs] = useState<any[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let mounted = true;
    let intId: any;
    const load = async () => {
      try {
        setLoading(true); setError(null);
        const list = await auditService.getRecentLogs(100);
        if (mounted) setLogs(list);
      } catch (e: any) {
        if (mounted) setError(typeof e?.message === 'string' ? e.message : 'Failed to load audit logs');
      } finally {
        if (mounted) setLoading(false);
      }
    };
    load();
    intId = setInterval(load, 10000);
    return () => { mounted = false; if (intId) clearInterval(intId); };
  }, []);

  return (
    <div className="space-y-6">
      <Card className="bg-white/40 dark:bg-slate-900/50 backdrop-blur-sm border border-white/30 dark:border-slate-700/40">
        <CardHeader className="flex items-center justify-between">
          <CardTitle>System Activity</CardTitle>
          <div className="flex items-center gap-2">
            <CardDescription className="mr-2">Monitor all system activities and user actions</CardDescription>
            <Button size="sm" variant="destructive" onClick={async () => {
              try {
                await auditService.deleteAllLogs();
                const list = await auditService.getRecentLogs(100);
                setLogs(list);
              } catch {}
            }}>Delete All</Button>
          </div>
        </CardHeader>
        <CardContent>
          {error ? (
            <div className="text-center py-8 text-muted-foreground">{error}</div>
          ) : loading ? (
            <div className="text-center py-8 text-muted-foreground">Loading...</div>
          ) : logs.length === 0 ? (
            <div className="text-center py-8 text-muted-foreground">
              <Shield className="mx-auto h-12 w-12 mb-4" />
              <p>No audit logs yet.</p>
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="text-left text-muted-foreground">
                    <th className="py-2 pr-4">Time</th>
                    <th className="py-2 pr-4">User</th>
                    <th className="py-2 pr-4">Action</th>
                    <th className="py-2 pr-4">Item</th>
                    <th className="py-2 pr-4">Claim</th>
                    <th className="py-2 pr-4">Details</th>
                  </tr>
                </thead>
                <tbody>
                  {logs.map((l, idx) => (
                    <tr key={idx} className="border-t">
                      <td className="py-2 pr-4">{new Intl.DateTimeFormat('en-IN', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit', second: '2-digit', hour12: false, timeZone: 'Asia/Kolkata' }).format(new Date(l.created_at || Date.now()))}</td>
                      <td className="py-2 pr-4">{l.user_id ?? '—'}</td>
                      <td className="py-2 pr-4">{l.action}</td>
                      <td className="py-2 pr-4">{l.item_id ?? '—'}</td>
                      <td className="py-2 pr-4">{l.claim_id ?? '—'}</td>
                      <td className="py-2 pr-4">{l.details ?? '—'}</td>
                      <td className="py-2 pr-4 text-right">
                        <Button size="sm" variant="destructive" onClick={async () => {
                          try {
                            const id = (l as any).id;
                            if (!id) return;
                            await auditService.deleteLogById(id);
                            setLogs(prev => prev.filter(x => (x as any).id !== id));
                          } catch {}
                        }}>Delete</Button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
};

type AnalyticsMetrics = { totalItems: number; totalUsers: number; activeClaims: number; activeChats: number };

type ItemsOverTime = { labels: string[]; items: number[] };
type ClaimStatus = { [k: string]: number };
type UserActivity = { active?: number; banned?: number };
type ChatActivity = { open?: number; closed?: number };
type ItemTypeBreakdown = { [k: string]: number };

const AnalyticsPage = ({ metrics, loading, error, itemsOverTime, claimStatus, userActivity, chatActivity, itemType }: { metrics: AnalyticsMetrics; loading: boolean; error: string | null; itemsOverTime: ItemsOverTime | null; claimStatus: ClaimStatus | null; userActivity: UserActivity | null; chatActivity: ChatActivity | null; itemType: ItemTypeBreakdown | null }) => (
  <div className="space-y-6">
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
      <Card className="bg-white/60 dark:bg-slate-900/60 backdrop-blur-sm border border-white/30 dark:border-slate-700/40">
        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
          <CardTitle className="text-sm font-medium">Total Items</CardTitle>
          <Package className="h-4 w-4 text-muted-foreground" />
        </CardHeader>
        <CardContent>
          <div className="text-2xl font-bold">{loading ? '—' : metrics.totalItems}</div>
          <p className="text-xs text-muted-foreground">Items reported</p>
        </CardContent>
      </Card>
      <Card className="bg-white/60 dark:bg-slate-900/60 backdrop-blur-sm border border-white/30 dark:border-slate-700/40">
        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
          <CardTitle className="text-sm font-medium">Total Users</CardTitle>
          <Users className="h-4 w-4 text-muted-foreground" />
        </CardHeader>
        <CardContent>
          <div className="text-2xl font-bold">{loading ? '—' : metrics.totalUsers}</div>
          <p className="text-xs text-muted-foreground">Registered users</p>
        </CardContent>
      </Card>
      <Card className="bg-white/60 dark:bg-slate-900/60 backdrop-blur-sm border border-white/30 dark:border-slate-700/40">
        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
          <CardTitle className="text-sm font-medium">Active Claims</CardTitle>
          <FileText className="h-4 w-4 text-muted-foreground" />
        </CardHeader>
        <CardContent>
          <div className="text-2xl font-bold">{loading ? '—' : metrics.activeClaims}</div>
          <p className="text-xs text-muted-foreground">Pending claims</p>
        </CardContent>
      </Card>
      <Card className="bg-white/60 dark:bg-slate-900/60 backdrop-blur-sm border border-white/30 dark:border-slate-700/40">
        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
          <CardTitle className="text-sm font-medium">Chat Sessions</CardTitle>
          <MessageCircle className="h-4 w-4 text-muted-foreground" />
        </CardHeader>
        <CardContent>
          <div className="text-2xl font-bold">{loading ? '—' : metrics.activeChats}</div>
          <p className="text-xs text-muted-foreground">Active chats</p>
        </CardContent>
      </Card>
    </div>
    <div className="grid grid-cols-1 gap-6">
      <Card className="bg-white/50 dark:bg-slate-900/60 backdrop-blur-sm border border-white/30 dark:border-slate-700/40">
        <CardHeader>
          <CardTitle>Items Over Time</CardTitle>
          <CardDescription>Last period</CardDescription>
        </CardHeader>
        <CardContent>
          {!itemsOverTime || itemsOverTime.labels.length === 0 ? (
            <div className="text-center py-8 text-muted-foreground">{error ?? 'No data yet'}</div>
          ) : (
            <div className="w-full overflow-x-auto">
              {(() => {
                const labels = itemsOverTime.labels;
                const values = itemsOverTime.items;
                const max = Math.max(1, ...values);
                const width = Math.max(360, labels.length * 48 + 40);
                const height = 220;
                const chartLeft = 40;
                const chartBottom = 24;
                const chartTop = 12;
                const chartWidth = width - chartLeft - 12;
                const chartHeight = height - chartTop - chartBottom;
                const barW = Math.max(16, chartWidth / Math.max(1, labels.length) - 12);
                return (
                  <svg width={width} height={height} className="block">
                    {/* axes */}
                    <line x1={chartLeft} y1={chartTop} x2={chartLeft} y2={chartTop + chartHeight} stroke="currentColor" className="text-slate-300 dark:text-slate-700" />
                    <line x1={chartLeft} y1={chartTop + chartHeight} x2={chartLeft + chartWidth} y2={chartTop + chartHeight} stroke="currentColor" className="text-slate-300 dark:text-slate-700" />
                    {/* bars */}
                    {values.map((v, i) => {
                      const x = chartLeft + i * (chartWidth / labels.length) + ((chartWidth / labels.length) - barW) / 2;
                      const h = Math.round((v / max) * chartHeight);
                      const y = chartTop + chartHeight - h;
                      return (
                        <g key={i}>
                          <rect x={x} y={y} width={barW} height={h} rx={4} className="fill-electric/90" />
                          <text x={x + barW / 2} y={chartTop + chartHeight + 14} textAnchor="middle" className="fill-slate-500 dark:fill-slate-400 text-[10px]">{labels[i]}</text>
                          <text x={x + barW / 2} y={y - 4} textAnchor="middle" className="fill-slate-700 dark:fill-slate-200 text-[10px]">{v}</text>
                        </g>
                      );
                    })}
                  </svg>
                );
              })()}
            </div>
          )}
        </CardContent>
      </Card>
      
    </div>

    {/* Additional pies: Claim Status */}
    <div className="grid grid-cols-1 gap-6">
      <Card className="bg-white/50 dark:bg-slate-900/60 backdrop-blur-sm border border-white/30 dark:border-slate-700/40">
        <CardHeader>
          <CardTitle>Claim Status</CardTitle>
          <CardDescription>Breakdown of claim outcomes</CardDescription>
        </CardHeader>
        <CardContent>
          {!claimStatus || Object.keys(claimStatus).length === 0 ? (
            <div className="text-center py-8 text-muted-foreground">{error ?? 'No data yet'}</div>
          ) : (
            <div className="flex items-center gap-6">
              {(() => {
                const entries = Object.entries(claimStatus) as [string, number][];
                const colors = ['#22c55e','#f97316','#ef4444','#06b6d4','#a78bfa','#eab308'];
                const total = Math.max(0, entries.reduce((s,[,v]) => s + (v||0), 0));
                const size = 200;
                const cx = size/2, cy = size/2; const r = 72; const sw = 24;
                const circ = 2 * Math.PI * r;
                let cum = 0;
                return (
                  <>
                    <svg width={size} height={size} aria-label="Claim status pie">
                      <g transform={`rotate(-90 ${cx} ${cy})`}>
                        <circle cx={cx} cy={cy} r={r} fill="none" stroke="#e5e7eb" strokeWidth={sw} />
                        {entries.map(([k,v], idx) => {
                          const frac = total === 0 ? 0 : (v || 0) / total;
                          const dash = frac * circ;
                          const offset = cum * circ;
                          cum += frac;
                          return (
                            <circle key={k} cx={cx} cy={cy} r={r} fill="none" stroke={colors[idx % colors.length]} strokeWidth={sw} strokeDasharray={`${dash} ${circ - dash}`} strokeDashoffset={-offset} />
                          );
                        })}
                      </g>
                    </svg>
                    <div className="space-y-2">
                      {entries.map(([k,v], idx) => (
                        <div key={k} className="flex items-center gap-2 text-sm">
                          <span className="inline-block w-3 h-3 rounded-sm" style={{ backgroundColor: colors[idx % colors.length] }} />
                          <span className="uppercase">{k}</span>
                          <span className="ml-auto text-muted-foreground">{v}</span>
                        </div>
                      ))}
                    </div>
                  </>
                );
              })()}
            </div>
          )}
        </CardContent>
      </Card>
    </div>

    {/* Creative: Rings and KPI visuals */}
    <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <Card className="bg-white/50 dark:bg-slate-900/60 backdrop-blur-sm border border-white/30 dark:border-slate-700/40">
        <CardHeader>
          <CardTitle>Return Rate</CardTitle>
          <CardDescription>% of claims marked RETURNED</CardDescription>
        </CardHeader>
        <CardContent>
          {!claimStatus ? (
            <div className="text-center py-8 text-muted-foreground">{error ?? 'No data yet'}</div>
          ) : (
            <div className="flex items-center gap-6">
              {(() => {
                const total = Object.values(claimStatus).reduce((s, v) => s + (v || 0), 0) || 0;
                const returned = (claimStatus['RETURNED'] ?? claimStatus['returned'] ?? 0) as number;
                const pct = total === 0 ? 0 : Math.round((returned / total) * 100);
                const size = 168; const cx = size/2, cy = size/2; const r = 58; const sw = 24; const circ = 2*Math.PI*r;
                const dash = (pct/100) * circ;
                return (
                  <>
                    <svg width={size} height={size} aria-label="Return rate">
                      <g transform={`rotate(-90 ${cx} ${cy})`}>
                        <circle cx={cx} cy={cy} r={r} fill="none" stroke="#e5e7eb" strokeWidth={sw} />
                        <circle cx={cx} cy={cy} r={r} fill="none" stroke="#22c55e" strokeWidth={sw} strokeDasharray={`${dash} ${circ - dash}`} />
                      </g>
                      <text x={cx} y={cy+4} textAnchor="middle" className="fill-slate-900 dark:fill-white text-[18px] font-semibold">{pct}%</text>
                    </svg>
                    <div className="text-sm text-muted-foreground">
                      <div><span className="text-foreground font-medium">{returned}</span> returned</div>
                      <div><span className="text-foreground font-medium">{total}</span> total claims</div>
                    </div>
                  </>
                );
              })()}
            </div>
          )}
        </CardContent>
      </Card>
      <Card className="bg-white/50 dark:bg-slate-900/60 backdrop-blur-sm border border-white/30 dark:border-slate-700/40">
        <CardHeader>
          <CardTitle>Active Claims Load</CardTitle>
          <CardDescription>Active vs Total Claims</CardDescription>
        </CardHeader>
        <CardContent>
          {!claimStatus ? (
            <div className="text-center py-8 text-muted-foreground">{error ?? 'No data yet'}</div>
          ) : (
            <div className="flex items-center gap-6">
              {(() => {
                const total = Object.values(claimStatus).reduce((s, v) => s + (v || 0), 0) || 0;
                const active = total - ((claimStatus['RETURNED'] ?? claimStatus['returned'] ?? 0) as number);
                const pct = total === 0 ? 0 : Math.round((active / total) * 100);
                const size = 168; const cx = size/2, cy = size/2; const r = 58; const sw = 24; const circ = 2*Math.PI*r;
                const dash = (pct/100) * circ;
                return (
                  <>
                    <svg width={size} height={size} aria-label="Active claims load">
                      <g transform={`rotate(-90 ${cx} ${cy})`}>
                        <circle cx={cx} cy={cy} r={r} fill="none" stroke="#e5e7eb" strokeWidth={sw} />
                        <circle cx={cx} cy={cy} r={r} fill="none" stroke="#f97316" strokeWidth={sw} strokeDasharray={`${dash} ${circ - dash}`} />
                      </g>
                      <text x={cx} y={cy+4} textAnchor="middle" className="fill-slate-900 dark:fill-white text-[18px] font-semibold">{pct}%</text>
                    </svg>
                    <div className="text-sm text-muted-foreground">
                      <div><span className="text-foreground font-medium">{active}</span> active</div>
                      <div><span className="text-foreground font-medium">{total}</span> total claims</div>
                    </div>
                  </>
                );
              })()}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  </div>
)
;

const AdminDashboard: React.FC = () => {
  const [activeTab, setActiveTab] = useState('');
  const location = useLocation();
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const { user, logout } = useAuth();
  const { isPreviewMode, currentRole, switchRole } = usePreviewMode();
  const { isDark, toggleTheme } = useTheme();
  const navigate = useNavigate();
  const [metrics, setMetrics] = useState<AnalyticsMetrics>({ totalItems: 0, totalUsers: 0, activeClaims: 0, activeChats: 0 });
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [users, setUsers] = useState<User[]>([]);
  const [usersLoading, setUsersLoading] = useState<boolean>(false);
  const [usersError, setUsersError] = useState<string | null>(null);
  const [adminItems, setAdminItems] = useState<Item[]>([]);
  const [itemsLoading, setItemsLoading] = useState<boolean>(false);
  const [itemsError, setItemsError] = useState<string | null>(null);
  const [pendingClaims, setPendingClaims] = useState<Claim[]>([]);
  const [claimsLoading, setClaimsLoading] = useState<boolean>(false);
  const [claimsError, setClaimsError] = useState<string | null>(null);
  const [chats, setChats] = useState<ChatSession[]>([]);
  const [chatsLoading, setChatsLoading] = useState<boolean>(false);
  const [chatsError, setChatsError] = useState<string | null>(null);
  const [activeChatId, setActiveChatId] = useState<number | null>(null);
  const [chatMessages, setChatMessages] = useState<ChatMessage[]>([]);
  const [chatParticipants, setChatParticipants] = useState<ChatParticipant[]>([]);
  const [userNames, setUserNames] = useState<Record<number, string>>({});
  const [notifications, setNotifications] = useState<AppNotification[]>([]);
  const [notifsLoading, setNotifsLoading] = useState<boolean>(false);
  const [notifsError, setNotifsError] = useState<string | null>(null);
  const [unreadCount, setUnreadCount] = useState<number>(0);
  const [itemsOverTime, setItemsOverTime] = useState<ItemsOverTime | null>(null);
  const [claimStatus, setClaimStatus] = useState<ClaimStatus | null>(null);
  const [userActivity, setUserActivity] = useState<UserActivity | null>(null);
  const [chatActivity, setChatActivity] = useState<ChatActivity | null>(null);
  const [imageUrl, setImageUrl] = useState<string | null>(null);
  const [detailsItem, setDetailsItem] = useState<Item | null>(null);
  const [itemType, setItemType] = useState<ItemTypeBreakdown | null>(null);
  const [adminDash, setAdminDash] = useState<{ itemsByMonth?: Record<string, number>; topLocations?: Array<{location: string; count: number}>; userGrowth?: Record<string, number>; claimStats?: Record<string, number> } | null>(null);
  const chatScrollRef = useRef<HTMLDivElement | null>(null);
  const [stickToBottom, setStickToBottom] = useState<boolean>(true);

  // Poll selected chat to watch live messages
  useEffect(() => {
    let mounted = true;
    let timer: any;
    async function loadChat() {
      if (!activeChatId) return;
      try {
        const { messages, participants } = await chatsService.getChatDetail(activeChatId);
        if (!mounted) return;
        setChatMessages(messages);
        setChatParticipants(participants);
        // Load usernames for participants
        try {
          const all = await usersService.getAllUsers();
          const map: Record<number, string> = {};
          for (const p of participants) {
            const u = all.find(x => x.id === p.user_id);
            if (u) map[p.user_id] = u.username;
          }
          setUserNames(map);
        } catch {}
      } catch (e) {
        // ignore transient errors
      }
    }
    if (activeChatId) {
      loadChat();
      timer = setInterval(loadChat, 2000);
    }
    return () => { mounted = false; if (timer) clearInterval(timer); };
  }, [activeChatId]);

  // Auto-scroll chat to latest when messages update, but only if near bottom or just opened
  useEffect(() => {
    if (activeChatId && chatScrollRef.current) {
      const el = chatScrollRef.current;
      if (stickToBottom || Math.abs(el.scrollHeight - (el.scrollTop + el.clientHeight)) < 40) {
        el.scrollTop = el.scrollHeight;
      }
    }
  }, [activeChatId, chatMessages, stickToBottom]);

  // Load all notifications (admin view)
  const reloadNotifications = async () => {
    setNotifsLoading(true); setNotifsError(null);
    try {
      const list = await notificationsService.getAllNotifications();
      setNotifications(list);
      setUnreadCount(list.filter(n => !n.is_read).length);
    } catch (e: any) {
      setNotifsError(typeof e?.message === 'string' ? e.message : 'Failed to load notifications');
    } finally {
      setNotifsLoading(false);
    }
  };

  const navigation = [
    { id: 'users', name: 'Users', icon: Users, href: '/admin/users' },
    { id: 'analytics', name: 'Analytics', icon: BarChart3, href: '/admin/analytics' },
    { id: 'items', name: 'Items', icon: Package, href: '/admin/items' },
    { id: 'claims', name: 'Claims', icon: FileText, href: '/admin/claims' },
    { id: 'chats', name: 'Chats', icon: MessageCircle, href: '/admin/chats' },
    { id: 'audit', name: 'Audit Logs', icon: Shield, href: '/admin/audit-logs' },
    { id: 'notifications', name: 'Notifications', icon: Bell, href: '/admin/notifications' },
  ];

  // keep activeTab in sync with the URL
  useEffect(() => {
    const seg = location.pathname.split('/')[2] || '';
    if (!seg) { setActiveTab(''); return; }
    const known = navigation.find(n => n.href.endsWith(seg));
    if (known && known.id !== activeTab) setActiveTab(known.id);
  }, [location.pathname]);

  // Load Admin Analytics from backend
  const loadAdminAnalytics = async () => {
    try {
      setLoading(true); setError(null);
      const res = await adminAnalytics.getAdminDashboard();
      // KPI metrics
      setMetrics({
        totalItems: Number(res.totalItems || 0),
        totalUsers: Number(res.totalUsers || 0),
        activeClaims: Number(res.totalClaims || 0),
        activeChats: Number(res.totalChats || 0),
      });
      // Claim status pie
      const cs = res.claimStats || {};
      setClaimStatus(Object.keys(cs).length ? cs : null);
      // Items over time (bar)
      const ibm = res.itemsByMonth || {};
      const labels = Object.keys(ibm);
      const items = labels.map(k => Number((ibm as any)[k] || 0));
      setItemsOverTime(labels.length ? { labels, items } : null);
    } catch (e: any) {
      setError(typeof e?.message === 'string' ? e.message : 'Failed to load analytics');
    } finally {
      setLoading(false);
    }
  };

  // Trigger analytics load when navigating to Analytics
  useEffect(() => {
    const isAnalytics = location.pathname.includes('/admin/analytics') || activeTab === 'analytics';
    if (isAnalytics) {
      loadAdminAnalytics();
    }
  }, [activeTab, location.pathname]);

  useEffect(() => {
    let mounted = true;
    async function load() {
      setLoading(true);
      setError(null);
      try {
        const data = await analytics.getDashboard();
        if (mounted) setMetrics(data);
      } catch (e: any) {
        if (mounted) setError(typeof e?.message === 'string' ? e.message : 'Failed to load analytics');
      } finally {
        if (mounted) setLoading(false);
      }
    }
    load();
    return () => { mounted = false; };
  }, []);

  // Load data when a section tab becomes active
  useEffect(() => {
    let mounted = true;
    const loadUsers = async () => {
      setUsersLoading(true); setUsersError(null);
      try { const list = await usersService.getAllUsers(); if (mounted) setUsers(list); }
      catch (e: any) { if (mounted) setUsersError(typeof e?.message === 'string' ? e.message : 'Failed to load users'); }
      finally { if (mounted) setUsersLoading(false); }
    };
    const loadItems = async () => {
      setItemsLoading(true); setItemsError(null);
      try { const list = await itemsService.getAdminItems(); if (mounted) setAdminItems(list); }
      catch (e: any) { if (mounted) setItemsError(typeof e?.message === 'string' ? e.message : 'Failed to load items'); }
      finally { if (mounted) setItemsLoading(false); }
    };
    const loadClaims = async () => {
      setClaimsLoading(true); setClaimsError(null);
      try { const list = await claimsService.getPendingClaims(); if (mounted) setPendingClaims(list); }
      catch (e: any) { if (mounted) setClaimsError(typeof e?.message === 'string' ? e.message : 'Failed to load claims'); }
      finally { if (mounted) setClaimsLoading(false); }
    };
    const loadChats = async () => {
      setChatsLoading(true); setChatsError(null);
      try { const list = await chatsService.getAllChats(user?.id ?? 0); if (mounted) setChats(list); }
      catch (e: any) { if (mounted) setChatsError(typeof e?.message === 'string' ? e.message : 'Failed to load chats'); }
      finally { if (mounted) setChatsLoading(false); }
    };

    if (activeTab === 'users') loadUsers();
    if (activeTab === 'items') loadItems();
    if (activeTab === 'claims') loadClaims();
    if (activeTab === 'chats') loadChats();
    if (activeTab === 'notifications') reloadNotifications();

    return () => { mounted = false; };
  }, [activeTab, user?.id]);

  // ...

  const isHome = (
    location.pathname === '/admin' ||
    location.pathname.endsWith('/admin') ||
    location.pathname === '/admin/home' ||
    location.pathname.endsWith('/admin/home')
  );

  return (
    <div className="min-h-screen relative">
      <div className="fixed inset-0 -z-10 bg-cover bg-center bg-no-repeat bg-fixed" style={{ backgroundImage: "url(/srm-campus.jpg)" }} />
      <div className="fixed inset-0 -z-10 bg-black/20" />
      {/* Main content */}
      <div>
        {/* Top bar */}
        <header className="bg-white/65 dark:bg-slate-900/75 backdrop-blur-md border border-white/20 dark:border-slate-800/30 shadow-sm">
          <div className="flex items-center justify-between h-16 px-6 relative">
            <div className="flex items-center space-x-4">
              <h1 className="absolute left-1/2 -translate-x-1/2 text-2xl font-bold font-poppins text-slate-900 dark:text-white">
                {isHome ? 'Home' : (navigation.find(nav => nav.id === activeTab)?.name || 'Dashboard')}
              </h1>
            </div>
            <div className="flex items-center space-x-2">
              {!isHome && (
                <Button variant="outline" size="sm" onClick={() => navigate('/admin')}>Home</Button>
              )}
              {isPreviewMode && !isHome && (
                <div className="flex items-center gap-2">
                  <div className="bg-amber/10 text-amber-800 dark:text-amber-200 px-3 py-1 rounded-full text-sm font-medium">
                    Preview Mode
                  </div>
                </div>
              )}
              <Button variant="ghost" size="icon" onClick={toggleTheme}>
                {isDark ? <Sun className="h-4 w-4" /> : <Moon className="h-4 w-4" />}
              </Button>
              <Button variant="outline" size="sm" onClick={logout}>Logout</Button>
            </div>
          </div>
        </header>

        {/* Page content */}
        {!isHome && isPreviewMode && (
          <div className="px-6 pt-4">
            <div className="w-full rounded-lg border border-amber/30 bg-amber/10 text-amber-900 dark:text-amber-100 px-4 py-2 text-sm font-medium">
              Preview Mode: Auth bypassed
            </div>
          </div>
        )}
        <main className={`p-6 w-full ${isHome ? 'pt-28 md:pt-32' : ''}`}>
          {isHome ? (
            <div className="max-w-5xl mx-auto">
              <div className="mt-4 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
              {navigation.map((item) => {
                const Icon = item.icon;
                return (
                  <button
                    key={item.id}
                    onClick={() => navigate(item.href)}
                    className="group flex flex-col items-center justify-center gap-3 p-8 rounded-[12px] bg-white/16 dark:bg-slate-900/20 border border-white/50 dark:border-slate-700/50 shadow-[0_1px_4px_rgba(0,0,0,0.06)] hover:bg-white/20 dark:hover:bg-slate-900/24 transition min-h-[160px] text-center font-sans font-semibold"
                  >
                    <span className="inline-flex h-14 w-14 items-center justify-center rounded-xl bg-[#1E3A8A] text-white shadow-md">
                      <Icon className="h-6 w-6" />
                    </span>
                    <span className="px-3 py-1 rounded bg-white/70 dark:bg-slate-900/60 text-sm font-semibold text-slate-900 dark:text-white">
                      {item.name}
                    </span>
                  </button>
                );
              })}
              </div>
            </div>
          ) : (
            <div className="max-w-5xl mx-auto pt-20 md:pt-24">
              {activeTab === 'analytics' && (
                <AnalyticsPage 
                  metrics={metrics} 
                  loading={loading} 
                  error={error} 
                  itemsOverTime={itemsOverTime} 
                  claimStatus={claimStatus} 
                  userActivity={userActivity} 
                  chatActivity={chatActivity} 
                  itemType={itemType} 
                />
              )}

              {activeTab === 'users' && (
                <UsersPage 
                  users={users} 
                  loading={usersLoading} 
                  error={usersError}
                  onBan={async (id) => {
                    try { await usersService.banUser(id, user?.id ?? 0); const list = await usersService.getAllUsers(); setUsers(list); } catch (_) {}
                  }}
                  onUnban={async (id) => {
                    try { await usersService.unbanUser(id, user?.id ?? 0); const list = await usersService.getAllUsers(); setUsers(list); } catch (_) {}
                  }}
                />
              )}

              {activeTab === 'items' && (
                <ItemsPage 
                  items={adminItems} 
                  loading={itemsLoading} 
                  error={itemsError}
                  onOpenImage={(url) => setImageUrl(url)}
                  onOpenDetails={(item) => setDetailsItem(item)}
                  onApprove={async (id) => { try { await itemsService.updateStatus(id, 'APPROVED'); const list = await itemsService.getAdminItems(); setAdminItems(list); } catch (_) {} }}
                  onReject={async (id) => { try { await itemsService.updateStatus(id, 'REJECTED'); const list = await itemsService.getAdminItems(); setAdminItems(list); } catch (_) {} }}
                />
              )}

              {activeTab === 'claims' && (
                <ClaimsPage 
                  claims={pendingClaims} 
                  loading={claimsLoading} 
                  error={claimsError}
                  onStartChat={async (claim) => {
                    try {
                      const chatId = await chatsService.createChat({ claimId: claim.id, startedBy: user?.id ?? 0, itemId: claim.item_id });
                      if (chatId) {
                        setActiveTab('chats');
                        setChatsLoading(true); setChatsError(null);
                        try {
                          const list = await chatsService.getAllChats(user?.id ?? 0);
                          setChats(list);
                        } catch (e: any) {
                          setChatsError(typeof e?.message === 'string' ? e.message : 'Failed to load chats');
                        } finally {
                          setChatsLoading(false);
                        }
                      } else {
                        alert('Failed to start chat');
                      }
                    } catch (e: any) {
                      alert(e?.message || 'Failed to start chat');
                    }
                  }}
                />
              )}

              {activeTab === 'chats' && (
                <ChatsPage 
                  sessions={chats} 
                  loading={chatsLoading} 
                  error={chatsError}
                  onClose={async (chatId) => {
                    try {
                      await chatsService.closeChat(chatId, user?.id ?? 0);
                      setChatsLoading(true); setChatsError(null);
                      const list = await chatsService.getAllChats(user?.id ?? 0);
                      setChats(list);
                    } catch (e: any) {
                      setChatsError(typeof e?.message === 'string' ? e.message : 'Failed to close chat');
                    } finally {
                      setChatsLoading(false);
                    }
                  }}
                  onDelete={async (chatId) => {
                    try {
                      await chatsService.deleteChat(chatId, user?.id ?? 0);
                      setChatsLoading(true); setChatsError(null);
                      const list = await chatsService.getAllChats(user?.id ?? 0);
                      setChats(list);
                    } catch (e: any) {
                      setChatsError(typeof e?.message === 'string' ? e.message : 'Failed to delete chat');
                    } finally {
                      setChatsLoading(false);
                    }
                  }}
                  onView={(id) => setActiveChatId(id)}
                />
              )}

              {activeTab === 'notifications' && (
                <NotificationsPage
                  notifications={notifications}
                  loading={notifsLoading}
                  error={notifsError}
                  onDelete={async (id) => {
                    const prev = notifications;
                    setNotifications(prev.filter(n => n.id !== id));
                    try {
                      await notificationsService.adminDeleteNotification(id, user?.id ?? 0);
                      await reloadNotifications();
                    } catch (e) {
                      setNotifications(prev);
                    }
                  }}
                />
              )}

              {activeTab === 'audit' && <AuditLogsPage />}
            </div>
          )}
        </main>

      </div>

      {/* Image Modal */}
      {imageUrl && (
        <div className="fixed inset-0 z-[100] bg-black/70 flex items-center justify-center p-4" onClick={() => setImageUrl(null)}>
          <img src={imageUrl || undefined} alt="Preview" className="max-w-full max-h-full rounded shadow-2xl" />
        </div>
      )}

      {/* Chat Viewer Modal - new standalone component */}
      {activeChatId && (
        <AdminChatViewer chatId={activeChatId} onClose={() => setActiveChatId(null)} />
      )}

      {/* Item Details Modal */}
      {detailsItem && (
        <div className="fixed inset-0 z-[100] bg-black/70 flex items-center justify-center p-4" onClick={() => setDetailsItem(null)}>
          <div className="bg-white dark:bg-slate-900 rounded-lg max-w-xl w-full p-4" onClick={(e) => e.stopPropagation()}>
            <div className="flex items-center justify-between mb-3">
              <h3 className="text-lg font-semibold">{detailsItem!.title}</h3>
              <Button size="sm" variant="ghost" onClick={() => setDetailsItem(null)}>Close</Button>
            </div>
            <div className="space-y-3">
              {detailsItem!.image_path && (
                <img src={fileUrl(detailsItem!.image_path as string)} alt={detailsItem!.title} className="w-full max-h-80 object-contain rounded" />
              )}
              <div className="text-sm"><span className="text-muted-foreground">Location:</span> {detailsItem!.location || '—'}</div>
              <div className="text-sm"><span className="text-muted-foreground">Status:</span> {detailsItem!.status || '—'}</div>
              <div className="text-sm"><span className="text-muted-foreground">Type:</span> {detailsItem!.type || '—'}</div>
              <div className="text-sm"><span className="text-muted-foreground">Description:</span></div>
              <div className="text-sm whitespace-pre-wrap border rounded p-2 bg-slate-50 dark:bg-slate-800">
                {detailsItem!.description || 'No description provided.'}
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default AdminDashboard;
