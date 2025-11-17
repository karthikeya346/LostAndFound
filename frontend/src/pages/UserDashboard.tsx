import React, { useEffect, useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { usePreviewMode } from '../contexts/PreviewModeContext';
import { useTheme } from '../contexts/ThemeContext';
import { Button } from '../components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../components/ui/card';
import { 
  Search, 
  Package, 
  FileText, 
  MessageCircle, 
  Bell, 
  User, 
  Moon, 
  Sun,
  Menu,
  X
} from 'lucide-react';
import itemsService from '../services/items';
import type { Item } from '../services/items';
import claimsService from '../services/claims';
import type { Claim } from '../services/claims';
import chatsService from '../services/chats';
import type { ChatSession, ChatMessage, ChatParticipant } from '../services/chats';
import notificationsService from '../services/notifications';
import type { Notification as AppNotification } from '../services/notifications';
import { fileUrl } from '../services/api';
import { Input } from '../components/ui/input';

// Items list with thumbnails and details
const ItemsPage = ({ items, loading, error, onOpenImage, onOpenDetails, onClaim, currentUserId }: { items: Item[]; loading: boolean; error: string | null; onOpenImage: (url: string) => void; onOpenDetails: (item: Item) => void; onClaim: (item: Item) => void; currentUserId: number }) => {
  const [q, setQ] = useState('');
  const filtered = React.useMemo(() => {
    const query = q.trim().toLowerCase();
    if (!query) return items;
    return items.filter((it: any) => {
      const fields = [it.title, it.description, it.location, it.type, it.status]
        .filter(Boolean)
        .map((s: any) => String(s).toLowerCase());
      return fields.some((f: string) => f.includes(query));
    });
  }, [q, items]);

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between gap-3">
        <h2 className="text-3xl font-bold font-poppins">Items</h2>
        <div className="flex items-center gap-2">
          <Input
            placeholder="Search items..."
            value={q}
            onChange={(e) => setQ(e.target.value)}
            className="w-64 max-w-[60%]"
          />
        </div>
      </div>
      <Card className="bg-white/40 dark:bg-slate-900/50 backdrop-blur-sm border border-white/30 dark:border-slate-700/40">
        <CardHeader>
          <CardTitle>Lost & Found Items</CardTitle>
          <CardDescription>
            Browse through reported lost and found items
          </CardDescription>
        </CardHeader>
        <CardContent>
          {error ? (
            <div className="text-center py-8 text-muted-foreground">{error}</div>
          ) : loading ? (
            <div className="text-center py-8 text-muted-foreground">Loading...</div>
          ) : filtered.length === 0 ? (
            <div className="text-center py-8 text-muted-foreground">
              <Package className="mx-auto h-12 w-12 mb-4" />
              <p>No items match your search.</p>
            </div>
          ) : (
            <div className="space-y-3">
              <div className="text-sm text-muted-foreground">Showing {filtered.length} of {items.length}</div>
              <ul className="divide-y">
                {filtered.slice(0, 12).map(it => {
                  const status = ((it.status as any) || '').toString().toUpperCase();
                  const isReturned = status === 'RETURNED';
                  return (
                    <li key={it.id} className={`py-3 flex items-center justify-between gap-4 ${isReturned ? 'opacity-70' : ''}`}>
                      <div className="flex items-center gap-3">
                        {it.image_path || (it as any).imagePath ? (
                          <img
                            src={fileUrl(it.image_path || (it as any).imagePath)}
                            alt={it.title}
                            className="w-16 h-16 rounded object-cover cursor-pointer"
                            onClick={() => onOpenImage(fileUrl((it.image_path || (it as any).imagePath)!))}
                          />
                        ) : (
                          <div className="w-16 h-16 rounded bg-slate-200 dark:bg-slate-800 flex items-center justify-center text-xs text-muted-foreground">No Image</div>
                        )}
                        <div className="relative">
                          <div className="font-medium flex items-center gap-2">
                            <span>{it.title}</span>
                            {isReturned && (
                              <span className="px-2 py-0.5 text-[10px] rounded-full bg-green-500/15 text-green-700 dark:text-green-300 border border-green-500/30">RETURNED</span>
                            )}
                          </div>
                          <div className="text-xs text-muted-foreground">{it.location} • {status || '—'}</div>
                          {isReturned && (
                            <span className="absolute -top-3 -left-2 rotate-[-10deg] bg-green-600 text-white text-[10px] px-2 py-0.5 rounded shadow">RETURNED</span>
                          )}
                        </div>
                      </div>
                      <div className="flex items-center gap-2">
                        <div className="text-xs text-muted-foreground hidden sm:block">{it.type}</div>
                        <Button size="sm" variant="outline" onClick={() => onOpenDetails(it)}>View Details</Button>
                        {it.type === 'FOUND' && (() => {
                          const ownerId = (it.userId ?? (it as any).user_id) as number | undefined;
                          const isSelfFound = !!ownerId && ownerId === currentUserId;
                          const disabled = isSelfFound || isReturned;
                          const title = isSelfFound ? 'You cannot claim your own FOUND item' : 'Claim this item';
                          return (
                            <Button size="sm" title={title} variant="default" disabled={disabled} onClick={() => onClaim(it)}>Claim</Button>
                          );
                        })()}
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
const ClaimsPage = ({ claims, loading, error, onStartChat }: { claims: Claim[]; loading: boolean; error: string | null; onStartChat: (c: Claim) => void }) => (
  <div className="space-y-6">
    <h2 className="text-3xl font-bold font-poppins">Claims</h2>
    <Card className="bg-white/40 dark:bg-slate-900/50 backdrop-blur-sm border border-white/30 dark:border-slate-700/40">
      <CardHeader>
        <CardTitle>My Claims</CardTitle>
        <CardDescription>
          Track your submitted claims and their status
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
            <p>No claims yet. Submit a claim on an item to get started!</p>
          </div>
        ) : (
          <div className="space-y-2">
            <div className="text-sm text-muted-foreground">Total: {claims.length}</div>
            <ul className="divide-y">
              {claims.slice(0, 12).map(c => (
                <li key={c.id} className="py-2 flex items-center justify-between">
                  <div>
                    <div className="font-medium">Claim #{c.id}</div>
                    <div className="text-xs text-muted-foreground">Item {c.item_id} • Status: {c.status}</div>
                  </div>
                  <div className="flex items-center gap-2">
                    {c.status === 'APPROVED' && (
                      <Button size="sm" variant="outline" onClick={() => onStartChat(c)}>Start Chat</Button>
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

const ChatsPage = ({ sessions, loading, error, onOpen }: { sessions: ChatSession[]; loading: boolean; error: string | null; onOpen: (chatId: number) => void }) => (
  <div className="space-y-6">
    <h2 className="text-3xl font-bold font-poppins">Chats</h2>
    <Card className="bg-white/40 dark:bg-slate-900/50 backdrop-blur-sm border border-white/30 dark:border-slate-700/40">
      <CardHeader>
        <CardTitle>Conversations</CardTitle>
        <CardDescription>
          Chat privately after a claim is approved
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
            <p>No active chats. Approved claims will create chat sessions!</p>
          </div>
        ) : (
          <ul className="divide-y">
            {sessions.map((s) => (
              <li key={s.id} className="py-3 flex items-center justify-between">
                <div>
                  <div className="font-medium text-sm">Chat #{s.id}</div>
                  <div className="text-xs text-muted-foreground">Status: {s.status || 'OPEN'}</div>
                </div>
                <Button size="sm" variant="outline" onClick={() => onOpen(s.id)}>Open</Button>
              </li>
            ))}
          </ul>
        )}
      </CardContent>
    </Card>
  </div>
);

const NotificationsPage = ({ notifications, loading, error, onDelete }: { notifications: AppNotification[]; loading: boolean; error: string | null; onDelete: (id: number) => void }) => (
  <div className="space-y-6">
    <h2 className="text-3xl font-bold font-poppins">Notifications</h2>
    <Card className="bg-white/40 dark:bg-slate-900/50 backdrop-blur-sm border border-white/30 dark:border-slate-700/40">
      <CardHeader>
        <CardTitle>Recent Notifications</CardTitle>
        <CardDescription>
          Stay updated with your claims, items, and sign-ins
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
            <p>No notifications yet.</p>
          </div>
        ) : (
          <ul className="divide-y">
            {notifications.map(n => (
              <li key={n.id} className="py-2 flex items-center justify-between">
                <div>
                  <div className="font-medium text-sm">{n.type || 'INFO'}</div>
                  <div className="text-xs text-muted-foreground">{n.message}</div>
                </div>
                <div className="flex items-center gap-2">
                  <Button size="sm" variant="destructive" onClick={() => onDelete(n.id)}>Delete</Button>
                </div>
              </li>
            ))}
          </ul>
        )}
      </CardContent>
    </Card>
  </div>
);

const ProfilePage = ({ username, email }: { username?: string; email?: string }) => (
  <div className="space-y-6">
    <h2 className="text-3xl font-bold font-poppins">Profile</h2>
    <Card className="bg-white/40 dark:bg-slate-900/50 backdrop-blur-sm border border-white/30 dark:border-slate-700/40">
      <CardHeader>
        <CardTitle>Account Information</CardTitle>
        <CardDescription>
          Manage your account settings and preferences
        </CardDescription>
      </CardHeader>
      <CardContent>
        <div className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="text-sm font-medium">Username</label>
              <p className="text-muted-foreground">{username || '—'}</p>
            </div>
            <div>
              <label className="text-sm font-medium">Email</label>
              <p className="text-muted-foreground">{email || '—'}</p>
            </div>
          </div>
        </div>
      </CardContent>
    </Card>
  </div>
);

const UserDashboard: React.FC = () => {
  const [activeTab, setActiveTab] = useState('items');
  const location = useLocation();
  const [showHub, setShowHub] = useState(() => location.pathname === '/user' || location.pathname.endsWith('/user'));
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const { user, logout } = useAuth();
  const { isPreviewMode, currentRole, switchRole } = usePreviewMode();
  const { isDark, toggleTheme } = useTheme();
  const navigate = useNavigate();
  const [items, setItems] = useState<Item[]>([]);
  const [itemsLoading, setItemsLoading] = useState<boolean>(false);
  const [itemsError, setItemsError] = useState<string | null>(null);
  const [imageUrl, setImageUrl] = useState<string | null>(null);
  const [detailsItem, setDetailsItem] = useState<Item | null>(null);
  const [claims, setClaims] = useState<Claim[]>([]);
  const [claimsLoading, setClaimsLoading] = useState<boolean>(false);
  const [claimsError, setClaimsError] = useState<string | null>(null);
  const [chats, setChats] = useState<ChatSession[]>([]);
  const [chatsLoading, setChatsLoading] = useState<boolean>(false);
  const [chatsError, setChatsError] = useState<string | null>(null);
  const [notifs, setNotifs] = useState<AppNotification[]>([]);
  const [notifsLoading, setNotifsLoading] = useState<boolean>(false);
  const [notifsError, setNotifsError] = useState<string | null>(null);
  const [activeChatId, setActiveChatId] = useState<number | null>(null);
  const [chatMessages, setChatMessages] = useState<ChatMessage[]>([]);
  const [chatParticipants, setChatParticipants] = useState<ChatParticipant[]>([]);
  const [chatInput, setChatInput] = useState<string>('');
  const [activeChatStatus, setActiveChatStatus] = useState<string>('OPEN');
  const [returnedPoll, setReturnedPoll] = useState<{ open: boolean; chatId?: number }>(
    { open: false }
  );
  const [reportOpen, setReportOpen] = useState(false);
  const [reportLoading, setReportLoading] = useState(false);
  const [reportTitle, setReportTitle] = useState('');
  const [reportDesc, setReportDesc] = useState('');
  const [reportLocation, setReportLocation] = useState('');
  const [reportType, setReportType] = useState<'LOST' | 'FOUND'>('LOST');
  const [reportFile, setReportFile] = useState<File | null>(null);
  const [reportPreviewUrl, setReportPreviewUrl] = useState<string | null>(null);

  const navigation = [
    { id: 'items', name: 'Items', icon: Package, href: '/user/items' },
    { id: 'claims', name: 'Claims', icon: FileText, href: '/user/claims' },
    { id: 'chats', name: 'Chats', icon: MessageCircle, href: '/user/chats' },
    { id: 'notifications', name: 'Notifications', icon: Bell, href: '/user/notifications' },
    { id: 'profile', name: 'Profile', icon: User, href: '/user/profile' },
  ];


  const handleLogout = () => {
    logout();
  };

  useEffect(() => {
    // Manage preview object URL lifecycle
    if (reportFile) {
      const objUrl = URL.createObjectURL(reportFile);
      setReportPreviewUrl(objUrl);
      return () => {
        URL.revokeObjectURL(objUrl);
        setReportPreviewUrl(null);
      };
    } else {
      setReportPreviewUrl(null);
    }
  }, [reportFile]);

  // Show tiles only on /user root
  useEffect(() => {
    const onHub = location.pathname === '/user' || location.pathname.endsWith('/user');
    setShowHub(onHub);
  }, [location.pathname]);

  useEffect(() => {
    let mounted = true;
    async function loadUserItems() {
      setItemsLoading(true); setItemsError(null);
      try {
        const list = await itemsService.getUserItems();
        if (mounted) setItems(list);
      } catch (e: any) {
        if (mounted) setItemsError(typeof e?.message === 'string' ? e.message : 'Failed to load items');
      } finally {
        if (mounted) setItemsLoading(false);
      }
    }
    async function loadUserClaims() {
      if (!user?.id) return;
      setClaimsLoading(true); setClaimsError(null);
      try {
        const list = await claimsService.getClaimsByUser(user.id);
        if (mounted) setClaims(list);
      } catch (e: any) {
        if (mounted) setClaimsError(typeof e?.message === 'string' ? e.message : 'Failed to load claims');
      } finally {
        if (mounted) setClaimsLoading(false);
      }
    }
    if (activeTab === 'items') loadUserItems();
    if (activeTab === 'claims') loadUserClaims();
    if (activeTab === 'chats') {
      (async () => {
        setChatsLoading(true); setChatsError(null);
        try {
          const list = await chatsService.getUserChats(user?.id ?? 0);
          if (mounted) setChats(list);
        } catch (e: any) {
          if (mounted) setChatsError(typeof e?.message === 'string' ? e.message : 'Failed to load chats');
        } finally {
          if (mounted) setChatsLoading(false);
        }
      })();
    }
    if (activeTab === 'notifications') {
      (async () => {
        if (!user?.id) return;
        setNotifsLoading(true); setNotifsError(null);
        try { const list = await notificationsService.getNotificationsByUser(user.id); if (mounted) setNotifs(list); }
        catch (e: any) { if (mounted) setNotifsError(typeof e?.message === 'string' ? e.message : 'Failed to load notifications'); }
        finally { if (mounted) setNotifsLoading(false); }
      })();
    }
    return () => { mounted = false; };
  }, [activeTab]);

  useEffect(() => {
    let mounted = true;
    (async () => {
      if (!activeChatId) return;
      try {
        const detail = await chatsService.getChatDetail(activeChatId);
        if (mounted) {
          setChatMessages(detail.messages);
          setChatParticipants(detail.participants);
          const session = chats.find(c => c.id === activeChatId);
          setActiveChatStatus(session?.status || 'OPEN');
        }
      } catch (_) {}
    })();
    return () => { mounted = false; };
  }, [activeChatId]);

  useEffect(() => {
    if (activeTab !== 'chats') return;
    let alive = true;
    const tick = async () => {
      try {
        const list = await chatsService.getUserChats(user?.id ?? 0);
        if (!alive) return;
        setChats(list);
        if (activeChatId) {
          const detail = await chatsService.getChatDetail(activeChatId);
          if (!alive) return;
          setChatMessages(detail.messages);
          setChatParticipants(detail.participants);
          const session = list.find(c => c.id === activeChatId) || chats.find(c => c.id === activeChatId);
          setActiveChatStatus(session?.status || 'OPEN');
        }
      } catch (_) {}
    };
    tick();
    const id = setInterval(tick, 10000);
    return () => { alive = false; clearInterval(id); };
  }, [activeTab, activeChatId, user?.id]);

  return (
    <>
    <div className="min-h-screen relative">
      <div className="fixed inset-0 -z-10 bg-cover bg-center bg-no-repeat bg-fixed" style={{ backgroundImage: "url(/srm-campus.jpg)" }} />
      <div className="fixed inset-0 -z-10 bg-black/20" />
      {/* Top bar */}
      <header className="bg-white/75 dark:bg-slate-900/70 backdrop-blur border border-white/30 dark:border-slate-800/60">
        <div className="flex items-center justify-between h-16 px-6 relative">
          <div className="flex items-center space-x-4">
            <span />
            <h1 className="absolute left-1/2 -translate-x-1/2 text-2xl font-bold font-poppins">
              {showHub ? 'Home' : (navigation.find(nav => nav.id === activeTab)?.name || 'Dashboard')}
            </h1>
          </div>
          <div className="flex items-center space-x-2">
            {!showHub && (
              <Button variant="outline" size="sm" onClick={() => navigate('/user')}>Home</Button>
            )}
            <Button
              variant="default"
              size="sm"
              onClick={() => setReportOpen(true)}
              className="hidden sm:inline-flex"
            >
              Report Item
            </Button>
            {isPreviewMode && (
              <div className="flex items-center gap-2">
                <div className="bg-amber/10 text-amber-800 dark:text-amber-200 px-3 py-1 rounded-full text-sm font-medium">
                  Preview Mode
                </div>
                <div className="hidden sm:flex items-center gap-1">
                  <Button
                    variant={currentRole === 'USER' ? 'default' : 'outline'}
                    size="sm"
                    onClick={() => { switchRole('USER'); navigate('/user/items'); }}
                  >
                    User
                  </Button>
                  <Button
                    variant={currentRole === 'ADMIN' ? 'default' : 'outline'}
                    size="sm"
                    onClick={() => { switchRole('ADMIN'); navigate('/admin/analytics'); }}
                  >
                    Admin
                  </Button>
                </div>
              </div>
            )}
            <Button variant="ghost" size="icon" onClick={toggleTheme}>
              {isDark ? <Sun className="h-4 w-4" /> : <Moon className="h-4 w-4" />}
            </Button>
            <Button variant="outline" size="sm" onClick={handleLogout}>Logout</Button>
          </div>
        </div>
      </header>

        {/* Page content */}
        {/* Preview banner */}
        {isPreviewMode && (
          <div className="px-6 pt-4">
            <div className="w-full rounded-lg border border-amber/30 bg-amber/10 text-amber-900 dark:text-amber-100 px-4 py-2 text-sm font-medium">
              Preview Mode: Auth bypassed
            </div>
          </div>
        )}
        <main className={`p-6 w-full ${showHub ? 'pt-28 md:pt-32' : ''}`}>
          {/* Full-screen Tile Hub */}
          {showHub && (
            <div className="max-w-5xl mx-auto">
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-8">
                {navigation.map((item) => {
                  const Icon = item.icon;
                  return (
                    <button
                      key={item.id}
                      onClick={() => { setActiveTab(item.id); setShowHub(false); navigate(item.href); }}
                      className="group flex flex-col items-start gap-4 p-8 rounded-2xl border bg-white/16 dark:bg-slate-900/20 border-white/50 dark:border-slate-700/50 hover:bg-white/20 dark:hover:bg-slate-900/24 transition min-h-[180px]"
                    >
                      <span className="inline-flex h-14 w-14 items-center justify-center rounded-xl bg-electric text-white shadow">
                        <Icon className="h-6 w-6" />
                      </span>
                      <span className="px-3 py-1 rounded bg-white/70 dark:bg-slate-900/60 text-base font-semibold text-slate-900 dark:text-white">{item.name}</span>
                    </button>
                  );
                })}
              </div>
            </div>
          )}

          {/* Hide content while hub is visible */}
          <div className={showHub ? 'hidden' : 'block'}>
          <div className="max-w-5xl mx-auto pt-20 md:pt-24">

          {activeTab === 'items' && (
            <ItemsPage 
              items={items}
              loading={itemsLoading}
              error={itemsError}
              onOpenImage={(url) => setImageUrl(url)}
              onOpenDetails={(item) => setDetailsItem(item)}
              onClaim={async (it) => {
                try {
                  await claimsService.createClaim(it.id, user?.id ?? 0);
                  alert('Claim submitted');
                } catch (e: any) {
                  alert(e?.message || 'Failed to submit claim');
                }
              }}
              currentUserId={user?.id ?? 0}
            />
          )}
          {activeTab === 'claims' && (
            <ClaimsPage 
              claims={claims}
              loading={claimsLoading}
              error={claimsError}
              onStartChat={async (c) => {
                try {
                  const chatId = await chatsService.createChat({ claimId: c.id, startedBy: user?.id ?? 0 });
                  if (chatId) setActiveTab('chats');
                } catch (_) {}
              }}
            />
          )}
          {activeTab === 'chats' && (
            <>
              <ChatsPage 
                sessions={chats} 
                loading={chatsLoading} 
                error={chatsError}
                onOpen={(id) => setActiveChatId(id)}
              />
              {activeChatId && (
                <Card className="mt-4 bg-white/40 dark:bg-slate-900/50 backdrop-blur-sm border border-white/30 dark:border-slate-700/40">
                  <CardHeader>
                    <CardTitle>Chat #{activeChatId}</CardTitle>
                    <CardDescription>
                      {chatParticipants.length > 0 ? `${chatParticipants.length} participants` : 'Loading participants...'}
                    </CardDescription>
                  </CardHeader>
                  <CardContent>
                    {activeChatStatus === 'CLOSED' && (
                      <div className="mb-2 text-xs text-red-600">This chat is closed. You can no longer send messages.</div>
                    )}
                    <div className="h-64 overflow-y-auto border rounded p-2 bg-slate-50 dark:bg-slate-900">
                      {chatMessages.length === 0 ? (
                        <div className="text-center text-muted-foreground text-sm py-8">No messages yet</div>
                      ) : (
                        <ul className="space-y-2">
                          {chatMessages.map(m => {
                            const anyM: any = m as any;
                            const ts = anyM.sent_at || anyM.createdAt || anyM.sentAt || anyM.sent_at;
                            let dt = ts ? new Date(ts) : new Date();
                            if (isNaN(dt.getTime())) dt = new Date();
                            const fmt = new Intl.DateTimeFormat('en-IN', {
                              day: '2-digit', month: '2-digit', year: 'numeric',
                              hour: '2-digit', minute: '2-digit', second: '2-digit', hour12: false,
                              timeZone: 'Asia/Kolkata'
                            });
                            const timeStr = fmt.format(dt);
                            return (
                              <li key={m.id} className="text-sm">
                                <span className="text-muted-foreground">[{timeStr}]</span> {m.message}
                              </li>
                            );
                          })}
                        </ul>
                      )}
                    </div>
                    <div className="mt-3 flex gap-2 items-center">
                      <Input value={chatInput} disabled={activeChatStatus === 'CLOSED'} onChange={(e: React.ChangeEvent<HTMLInputElement>) => setChatInput(e.target.value)} placeholder="Type a message" />
                      <Button 
                        disabled={activeChatStatus === 'CLOSED'}
                        onClick={async () => {
                          if (!activeChatId || !chatInput.trim() || activeChatStatus === 'CLOSED') return;
                          try {
                            const ok = await chatsService.sendMessage(activeChatId, user?.id ?? 0, chatInput.trim());
                            if (ok) {
                              setChatInput('');
                              const detail = await chatsService.getChatDetail(activeChatId);
                              setChatMessages(detail.messages);
                            }
                          } catch (_) {}
                        }}
                      >Send</Button>
                      <Button 
                        variant="destructive"
                        onClick={async () => {
                          if (!activeChatId) return;
                          try {
                            const ok = await chatsService.closeChatByUser(activeChatId, user?.id ?? 0);
                            if (ok) {
                              setActiveChatStatus('CLOSED');
                              // refresh chats list to reflect closed status
                              const list = await chatsService.getUserChats(user?.id ?? 0);
                              setChats(list);
                              // open poll asking if item was returned
                              setReturnedPoll({ open: true, chatId: activeChatId });
                            }
                          } catch (_) {}
                        }}
                      >End Chat</Button>
                    </div>
                  </CardContent>
                </Card>
              )}
            </>
          )}
          {activeTab === 'notifications' && (
            <NotificationsPage 
              notifications={notifs}
              loading={notifsLoading}
              error={notifsError}
              onDelete={async (id) => {
                // Optimistic update
                const prev = notifs;
                setNotifs(prev.filter(n => n.id !== id));
                try {
                  await notificationsService.deleteNotification(id, user?.id ?? 0);
                  // final refresh to stay in sync
                  const list = await notificationsService.getNotificationsByUser(user?.id ?? 0);
                  setNotifs(list);
                } catch {
                  // rollback on failure
                  setNotifs(prev);
                }
              }}
            />
          )}
          {activeTab === 'profile' && <ProfilePage username={user?.username} email={user?.email} />}
          </div>
          </div>
        </main>
      </div>

    {/* Image Modal */}
    {imageUrl && (
      <div className="fixed inset-0 z-[100] bg-black/70 flex items-center justify-center p-4" onClick={() => setImageUrl(null)}>
        <img src={imageUrl!} alt="Preview" className="max-w-full max-h-full rounded shadow-2xl" />
      </div>
    )}

    {/* Item Details Modal */}
    {detailsItem && (
      <div className="fixed inset-0 z-[100] bg-black/70 flex items-center justify-center p-4" onClick={() => setDetailsItem(null)}>
        <div className="bg-white dark:bg-slate-900 rounded-lg max-w-xl w-full p-4" onClick={(e) => e.stopPropagation()}>
          <div className="flex items-center justify-between mb-3">
            <h3 className="text-lg font-semibold flex items-center gap-2">
              <span>{detailsItem!.title}</span>
              {detailsItem!.status === 'RETURNED' && (
                <span className="px-2 py-0.5 text-[10px] rounded-full bg-green-500/15 text-green-700 dark:text-green-300 border border-green-500/30">RETURNED</span>
              )}
            </h3>
            <Button size="sm" variant="ghost" onClick={() => setDetailsItem(null)}>Close</Button>
          </div>
          <div className="space-y-3">
            {detailsItem!.image_path && (
              <img src={fileUrl(detailsItem!.image_path!)} alt={detailsItem!.title} className="w-full max-h-80 object-contain rounded" />
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

    {/* Report Item Modal */}
    {reportOpen && (
      <div className="fixed inset-0 z-[100] bg-black/70 flex items-center justify-center p-4" onClick={() => !reportLoading && setReportOpen(false)}>
        <div className="bg-white dark:bg-slate-900 rounded-lg w-full max-w-xl p-4" onClick={(e) => e.stopPropagation()}>
          <div className="flex items-center justify-between mb-3">
            <h3 className="text-lg font-semibold">Report Item</h3>
            <Button size="sm" variant="ghost" onClick={() => !reportLoading && setReportOpen(false)}>Close</Button>
          </div>
          <div className="space-y-3">
            <div>
              <label className="text-sm">Title</label>
              <input className="mt-1 w-full px-3 py-2 rounded border bg-transparent" value={reportTitle} onChange={e => setReportTitle(e.target.value)} />
            </div>
            <div>
              <label className="text-sm">Description</label>
              <textarea className="mt-1 w-full px-3 py-2 rounded border bg-transparent" rows={3} value={reportDesc} onChange={e => setReportDesc(e.target.value)} />
            </div>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              <div>
                <label className="text-sm">Location</label>
                <input className="mt-1 w-full px-3 py-2 rounded border bg-transparent" value={reportLocation} onChange={e => setReportLocation(e.target.value)} />
              </div>
              <div>
                <label className="text-sm">Type</label>
                <select className="mt-1 w-full px-3 py-2 rounded border bg-transparent" value={reportType} onChange={e => setReportType(e.target.value as 'LOST' | 'FOUND')}>
                  <option value="LOST">LOST</option>
                  <option value="FOUND">FOUND</option>
                </select>
              </div>
            </div>
            <div>
              <label className="text-sm">Image</label>
              <input
                className="mt-1 w-full"
                type="file"
                accept="image/*"
                onChange={(e) => setReportFile(e.target.files && e.target.files[0] ? e.target.files[0] : null)}
              />
              {reportFile && (
                <div className="mt-1 text-xs text-muted-foreground">Selected: {reportFile.name}</div>
              )}
              {reportPreviewUrl && (
                <div className="mt-2">
                  <img
                    src={reportPreviewUrl}
                    alt="Preview"
                    className="w-full max-h-60 object-contain rounded cursor-pointer border"
                    onClick={() => setImageUrl(reportPreviewUrl!)}
                  />
                  <div className="text-xs text-muted-foreground mt-1">Click the image to view full size</div>
                </div>
              )}
            </div>
          </div>
          <div className="mt-4 flex justify-end gap-2">
            <Button type="button" variant="outline" disabled={reportLoading} onClick={() => setReportOpen(false)}>Cancel</Button>
            <Button
              type="button"
              disabled={reportLoading || !reportTitle.trim() || !reportFile}
              onClick={async () => {
                try {
                  if (!reportFile) {
                    alert('Please attach an image (photo is required).');
                    return;
                  }
                  setReportLoading(true);
                  const res = await itemsService.reportItemWithUpload({
                    userId: user?.id ?? 0,
                    title: reportTitle.trim(),
                    description: reportDesc,
                    location: reportLocation,
                    type: reportType,
                    file: reportFile,
                  });
                  const ok = !!res?.success;
                  if (ok) {
                    setReportOpen(false);
                    setReportTitle(''); setReportDesc(''); setReportLocation(''); setReportType('LOST'); setReportFile(null);
                    if (activeTab === 'items') {
                      // reload items to reflect pending item for the reporter's own list (if shown elsewhere)
                      // here we keep user view showing only APPROVED; so no visible change is expected
                      // but we refresh anyway
                      try { const list = await itemsService.getUserItems(); setItems(list); } catch {}
                    }
                  }
                } finally {
                  setReportLoading(false);
                }
              }}
            >
              {reportLoading ? 'Submitting...' : 'Submit'}
            </Button>
          </div>
        </div>
      </div>
    )}

    {/* Mandatory End-Chat Outcome Modal */}
    {returnedPoll.open && (
      <div className="fixed inset-0 z-[110] bg-black/70 flex items-center justify-center p-4">
        <div className="bg-white dark:bg-slate-900 rounded-lg max-w-sm w-full p-4" onClick={(e) => e.stopPropagation()}>
          {(() => {
            const chat = chats.find((c) => c.id === returnedPoll.chatId);
            const claimId = chat?.claimId || (chat as any)?.claim_id;
            const itemId = chat?.itemId || (chat as any)?.item_id;
            const item = items.find(i => i.id === itemId) as any;
            const itemType = (item?.type || '').toUpperCase();
            const isLost = itemType === 'LOST';
            const title = isLost ? 'Was the item claimed?' : 'Was the item returned?';
            const yesLabel = isLost ? 'Claimed' : 'Returned';
            const noLabel = isLost ? 'Not Claimed' : 'Not Returned';
            return (
              <>
                <div className="mb-3">
                  <h3 className="text-lg font-semibold">{title}</h3>
                  <div className="text-sm text-muted-foreground">Please choose one option to continue.</div>
                </div>
                <div className="flex gap-2">
                  <Button className="flex-1" onClick={async () => {
                    try {
                      if (claimId) { await claimsService.markReturned(claimId); }
                    } catch (_) {}
                    // refresh user chats so closed/hidden
                    try {
                      const list = await chatsService.getUserChats(user?.id ?? 0);
                      setChats(list);
                    } catch {}
                    // refresh items to reflect RETURNED status
                    try {
                      const itemsList = await itemsService.getUserItems();
                      setItems(itemsList);
                    } catch {}
                    setReturnedPoll({ open: false });
                  }}>{yesLabel}</Button>
                  <Button className="flex-1" variant="outline" onClick={async () => {
                    // Just ensure list is refreshed; chat already closed
                    try { const list = await chatsService.getUserChats(user?.id ?? 0); setChats(list); } catch {}
                    setReturnedPoll({ open: false });
                  }}>{noLabel}</Button>
                </div>
              </>
            );
          })()}
        </div>
      </div>
    )}
    </>
  );
};

export default UserDashboard;
