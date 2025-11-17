import React, { useEffect, useMemo, useRef, useState } from 'react';
import { Button } from '../components/ui/button';
import chatsService, { ChatMessage, ChatParticipant } from '../services/chats';
import usersService from '../services/users';

type Props = {
  chatId: number;
  onClose: () => void;
};

export default function AdminChatViewer({ chatId, onClose }: Props) {
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [participants, setParticipants] = useState<ChatParticipant[]>([]);
  const [userInfo, setUserInfo] = useState<Record<number, { username?: string; email?: string }>>({});
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  // scrolling state
  const scrollRef = useRef<HTMLDivElement | null>(null);
  const [stickToBottom, setStickToBottom] = useState<boolean>(true);

  // Load chat details (polling)
  useEffect(() => {
    let mounted = true;
    let timer: any;
    async function load() {
      if (!chatId) return;
      try {
        if (!messages.length) { setLoading(true); setError(null); }
        const { messages: m, participants: p } = await chatsService.getChatDetail(chatId);
        if (!mounted) return;
        setMessages(m);
        setParticipants(p);
        // fetch usernames/emails for participants
        try {
          const all = await usersService.getAllUsers();
          const map: Record<number, { username?: string; email?: string }> = {};
          for (const part of p) {
            const id = (typeof (part as any).user_id === 'number' ? (part as any).user_id : (typeof (part as any).userId === 'number' ? (part as any).userId : undefined)) as number | undefined;
            const u = id != null ? all.find(x => x.id === id) : undefined;
            if (u) map[id!] = { username: u.username, email: u.email };
          }
          // ensure we also cover sender IDs that may not be present in participants
          const senderIds = Array.from(new Set(m.map(x => (x as any).sender_user_id ?? (x as any).senderUserId).filter((n): n is number => typeof n === 'number' && !Number.isNaN(n))));
          const needIds = senderIds.filter(id => !map[id]);
          if (needIds.length > 0) {
            const fetched = await Promise.all(needIds.map(async (id) => ({ id, user: await usersService.getUserById(id) })));
            for (const f of fetched) {
              if (f.user) map[f.id] = { username: f.user.username, email: f.user.email };
            }
          }
          setUserInfo(map);
        } catch {}
      } catch (e: any) {
        if (mounted) setError(typeof e?.message === 'string' ? e.message : 'Failed to load chat');
      } finally {
        if (mounted) setLoading(false);
      }
    }
    load();
    timer = setInterval(load, 2000);
    return () => { mounted = false; if (timer) clearInterval(timer); };
  }, [chatId]);

  // Auto-scroll only when near bottom or on open
  useEffect(() => {
    if (!scrollRef.current) return;
    const el = scrollRef.current;
    if (stickToBottom || Math.abs(el.scrollHeight - (el.scrollTop + el.clientHeight)) < 40) {
      el.scrollTop = el.scrollHeight;
    }
  }, [messages, stickToBottom]);

  const maps = useMemo(() => {
    const byUser: Record<number, ChatParticipant> = Object.fromEntries((participants || [])
      .map(p => [typeof (p as any).user_id === 'number' ? (p as any).user_id : (typeof (p as any).userId === 'number' ? (p as any).userId : undefined), p] as const)
      .filter(([id]) => typeof id === 'number')) as any;
    const idsFromParticipants = Object.keys(byUser).map(n => parseInt(n, 10)).filter(n => !Number.isNaN(n));
    const idsFromMessages = Array.from(new Set(messages.map(m => (m as any).sender_user_id ?? (m as any).senderUserId).filter((n): n is number => typeof n === 'number' && !Number.isNaN(n))));
    const ids = Array.from(new Set([...idsFromParticipants, ...idsFromMessages]));
    const aliasById: Record<number, string> = {};
    const sorted = [...ids].sort((a, b) => a - b);
    sorted.forEach((id, idx) => { aliasById[id] = idx === 0 ? 'User' : `User ${idx + 1}`; });

    // Build alias by sender key (covers missing sender_user_id)
    const senderKey = (m: any): string => {
      const cand = m?.sender_user_id ?? m?.senderUserId ?? m?.sender_username ?? m?.sender_email ?? m?.senderRole ?? m?.role ?? m?.from_user_id ?? m?.fromUserId ?? m?.from;
      return String(cand ?? `idx:${m?.id ?? ''}`);
    };
    const order: string[] = [];
    const aliasByKey: Record<string, string> = {};
    messages.forEach((m, i) => {
      const key = senderKey(m);
      if (!order.includes(key)) order.push(key);
      const idx = order.indexOf(key);
      aliasByKey[key] = idx === 0 ? 'User' : `User ${idx + 1}`;
    });

    // Map sender keys to participant display names by order
    const pSorted = [...(participants || [])].sort((a, b) => {
      const ida = (typeof a.user_id === 'number' ? a.user_id : (a as any).user_id) as number | undefined;
      const idb = (typeof b.user_id === 'number' ? b.user_id : (b as any).user_id) as number | undefined;
      return (ida ?? 0) - (idb ?? 0);
    });
    const keyToParticipantId: Record<string, number | undefined> = {};
    order.forEach((k, idx) => {
      const pid = (typeof pSorted[idx]?.user_id === 'number' ? pSorted[idx]?.user_id : (pSorted[idx] as any)?.user_id) as number | undefined;
      keyToParticipantId[k] = pid;
    });

    const keyToPrettyName: Record<string, string> = {};
    order.forEach((k, idx) => {
      const pid = keyToParticipantId[k];
      if (pid != null) {
        const part = byUser[pid];
        const alias = aliasByKey[k];
        const pretty = (part?.alias && String(part.alias)) || alias; // usernames/emails added at render time where we have userInfo
        keyToPrettyName[k] = pretty;
      } else {
        keyToPrettyName[k] = aliasByKey[k];
      }
    });

    return { byUser, aliasById, aliasByKey, keyToParticipantId, keyToPrettyName, senderKey };
  }, [participants, messages]);

  // Helper: normalize various timestamp formats to a Date
  const toDate = (m: any): Date | null => {
    const raw = m?.sent_at ?? m?.sentAt ?? m?.created_at ?? m?.createdAt ?? m?.timestamp ?? m?.time ?? m?.createdAt; // include camelCase createdAt
    if (raw == null) return null;
    if (typeof raw === 'number') {
      // if seconds (10 digits), convert to ms
      const ms = raw < 1e12 ? raw * 1000 : raw;
      const d = new Date(ms);
      return isNaN(d.getTime()) ? null : d;
    }
    if (typeof raw === 'string') {
      // Some backends return 'YYYY-MM-DD HH:mm:ss' (no timezone). Treat as local time.
      // Replace space with 'T' so Date parses as local in most browsers.
      const s = raw.includes('T') || raw.endsWith('Z') ? raw : raw.replace(' ', 'T');
      const d = new Date(s);
      return isNaN(d.getTime()) ? null : d;
    }
    try {
      const d = new Date(raw);
      return isNaN(d.getTime()) ? null : d;
    } catch { return null; }
  };

  return (
    <div className="fixed inset-0 z-[110] bg-black/70 flex items-center justify-center p-4" onClick={onClose}>
      <div className="bg-white dark:bg-slate-900 rounded-lg max-w-2xl w-full p-4 flex flex-col" onClick={(e) => e.stopPropagation()}>
        <div className="flex items-center justify-between mb-3">
          <h3 className="text-lg font-semibold">Chat #{chatId}</h3>
          <div className="flex items-center gap-2">
            <Button size="sm" variant="outline" onClick={onClose}>Close Viewer</Button>
          </div>
        </div>
        {error && (
          <div className="text-sm text-red-600 dark:text-red-400 mb-2">{error}</div>
        )}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          <div className="md:col-span-3">
            <div
              ref={scrollRef}
              className="h-80 overflow-y-auto rounded border p-3 space-y-4 bg-white/60 dark:bg-slate-900/50"
              onScroll={(e) => {
                const el = e.currentTarget;
                const nearBottom = Math.abs(el.scrollHeight - (el.scrollTop + el.clientHeight)) < 40;
                setStickToBottom(nearBottom);
              }}
            >
              {loading && messages.length === 0 ? (
                <div className="text-center text-sm text-muted-foreground">Loading…</div>
              ) : messages.length === 0 ? (
                <div className="text-center text-sm text-muted-foreground">No messages yet</div>
              ) : (
                messages.map(m => {
                  const sid = (m as any).sender_user_id ?? (m as any).senderUserId ?? (m as any).from_user_id ?? (m as any).fromUserId;
                  const p = (sid != null) ? maps.byUser[sid] : undefined;
                  const isOwner = !!p?.role && p.role.includes('OWNER');
                  const isClaimant = !!p?.role && p.role.includes('CLAIMANT');
                  const isAdmin = !!p?.role && p.role.includes('ADMIN');
                  const key = maps.senderKey(m);
                  const idx = (maps as any).aliasByKey && (maps as any).aliasByKey[key] ? parseInt(((maps as any).aliasByKey[key].split(' ')[1] || '1'), 10) - 1 : 0;
                  // Alignment/color: prefer role; if no role, alternate by alias index
                  const align = isOwner ? 'items-end' : isAdmin ? 'items-end' : isClaimant ? 'items-start' : (idx % 2 === 0 ? 'items-start' : 'items-end');
                  const bubble = isOwner ? 'bg-blue-600 text-white' : (isAdmin ? 'bg-emerald-600 text-white' : (isClaimant ? 'bg-slate-200 dark:bg-slate-700 text-slate-900 dark:text-white' : (idx % 2 === 0 ? 'bg-slate-100 dark:bg-slate-800' : 'bg-indigo-600 text-white')));
                  const name = (sid != null && (userInfo[sid]?.username || userInfo[sid]?.email))
                    || (p?.alias && String(p.alias))
                    || (sid != null && maps.aliasById[sid])
                    || (maps.aliasByKey[key])
                    || (isOwner ? 'Owner' : (isClaimant ? 'Claimant' : (isAdmin ? 'Admin' : 'User')));
                  const roleChip = isOwner ? 'Owner' : (isClaimant ? 'Claimant' : (isAdmin ? 'Admin' : undefined));
                  const chipStyle = isOwner ? 'bg-blue-100 text-blue-700 dark:bg-blue-900/40 dark:text-blue-200' : (isAdmin ? 'bg-emerald-100 text-emerald-700 dark:bg-emerald-900/40 dark:text-emerald-200' : 'bg-slate-200 text-slate-800 dark:bg-slate-800 dark:text-slate-200');
                  const ts = toDate(m);
                  const tsClass = isOwner || isAdmin ? 'text-white/80 dark:text-slate-300/80' : 'text-slate-600 dark:text-slate-300/80';
                  return (
                    <div key={m.id} className={`text-sm flex flex-col ${align}`}>
                      <div className={`max-w-[85%] rounded-md shadow-sm ${bubble}`}>
                        <div className="px-3 pt-2">
                          <div className="flex items-center gap-2 mb-1">
                            <span className="font-semibold tracking-wide">{name}</span>
                            {roleChip && (
                              <span className={`text-[10px] px-2 py-[2px] rounded-full ${chipStyle}`}>{roleChip}</span>
                            )}
                          </div>
                          <div className="leading-relaxed whitespace-pre-wrap pb-2">{m.message}</div>
                        </div>
                        <div className={`px-3 pb-2 text-[11px] opacity-90 ${tsClass}`}>{ts ? ts.toLocaleString() : '—'}</div>
                      </div>
                    </div>
                  );
                })
              )}
            </div>
          </div>
          <div className="md:col-span-1">
            <div className="rounded border p-3 bg-white/60 dark:bg-slate-900/50">
              <div className="font-medium text-sm mb-2">Participants</div>
              <ul className="space-y-1 text-sm">
                {participants.length === 0 ? (
                  <li className="text-muted-foreground">No participants info</li>
                ) : (
                  participants.map(p => {
                    const id = (typeof (p as any).user_id === 'number' ? (p as any).user_id : (typeof (p as any).userId === 'number' ? (p as any).userId : undefined)) as number | undefined;
                    const role = p.role || '';
                    const name = (id != null && (userInfo[id]?.username || userInfo[id]?.email)) || (p.alias && String(p.alias)) || (id != null ? maps.aliasById[id] : undefined) || (role.includes('OWNER') ? 'Owner' : (role.includes('CLAIMANT') ? 'Claimant' : 'User'));
                    const chipStyle = role.includes('OWNER') ? 'bg-blue-100 text-blue-700 dark:bg-blue-900/40 dark:text-blue-200' : (role.includes('CLAIMANT') ? 'bg-slate-200 text-slate-800 dark:bg-slate-800 dark:text-slate-200' : 'bg-emerald-100 text-emerald-700 dark:bg-emerald-900/40 dark:text-emerald-200');
                    return (
                      <li key={p.id} className="flex items-center justify-between">
                        <span className="flex items-center gap-2">
                          <span>{name}</span>
                          <span className={`text-[10px] px-2 py-[2px] rounded-full ${chipStyle}`}>{role || 'user'}</span>
                        </span>
                        <span className="text-xs text-muted-foreground"></span>
                      </li>
                    );
                  })
                )}
              </ul>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
