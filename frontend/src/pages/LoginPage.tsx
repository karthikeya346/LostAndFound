import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { Button } from '../components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../components/ui/card';
import { Input } from '../components/ui/input';
import { Label } from '../components/ui/label';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '../components/ui/tabs';
import { Eye, EyeOff, Search } from 'lucide-react';
import { authService } from '../services/authService';

export default function LoginPage() {
  const navigate = useNavigate();
  const { login, verifyOtp, isLoading, user } = useAuth();
  
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [email, setEmail] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState('');
  const [otpStep, setOtpStep] = useState(false);
  const [otp, setOtp] = useState('');
  const [otpError, setOtpError] = useState('');
  const [pendingUserId, setPendingUserId] = useState<number | null>(null); // no longer used for verification
  const [otpPopup, setOtpPopup] = useState<{ open: boolean; value?: string }>({ open: false });
  const [forgotOpen, setForgotOpen] = useState(false);
  const [fpEmail, setFpEmail] = useState('');
  const [fpToken, setFpToken] = useState('');
  const [fpServerToken, setFpServerToken] = useState('');
  const [fpNewPwd, setFpNewPwd] = useState('');
  const [fpStep, setFpStep] = useState<'request' | 'reset'>('request');
  const [fpMsg, setFpMsg] = useState<string>('');

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    
    try {
      const res = await login(username, password);
      if (res.success && res.user) {
        setOtp('');
        setOtpError('');
        setOtpStep(true);
        if (res.otp) {
          // Native browser popup for reliability
          setTimeout(() => {
            window.alert(`Your One-Time Password\n\n${res.otp}\n\nUse this OTP within 5 minutes to complete sign in.`);
          }, 0);
        }
      } else {
        setError(res.message || 'Invalid username or password');
      }
    } catch (err) {
      setError('Login failed. Please try again.');
    }
  };

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    if (!username || !password || !email) {
      setError('Please fill in all fields');
      return;
    }
    const emailOk = email.toLowerCase().endsWith('@srmist.edu.in');
    if (!emailOk) {
      setError('Only @srmist.edu.in email addresses are allowed');
      return;
    }

    try {
      const response = await fetch('http://localhost:8080/api/auth/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password, email }),
      });

      const data = await response.json();
      if (data.success) {
        navigate('/user');
      } else {
        setError(data.message || 'Registration failed');
      }
    } catch (err) {
      setError('Registration failed. Please try again.');
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center lg:justify-start p-4 lg:pl-3 xl:pl-8 relative z-[1] font-sans">
      <div className="fixed inset-0 z-[1] bg-black/35 pointer-events-none"></div>
      <Card className="w-full max-w-md mt-12 lg:mt-28 bg-white/16 dark:bg-slate-900/20 backdrop-blur-sm border border-white/50 dark:border-slate-700/50 shadow-[0_14px_50px_-12px_rgba(0,0,0,0.55)] rounded-2xl relative z-[10]">
        <CardHeader className="space-y-1">
          <div className="flex items-center justify-center space-x-2 mb-4">
            <Search className="h-8 w-8 text-electric" />
            <CardTitle className="text-3xl font-bold font-poppins">Lost & Found</CardTitle>
          </div>

          <div className="space-y-1">
            <div className="text-base md:text-lg font-semibold text-center text-slate-900 dark:text-white">Welcome! Lost something? We’ve got you covered.</div>
          </div>
        </CardHeader>
        <CardContent>
          <Tabs defaultValue="login" className="w-full">
            <TabsList className="grid w-full grid-cols-2 rounded-full p-1 bg-white/16 dark:bg-slate-900/20 backdrop-blur-sm border border-white/40 dark:border-slate-700/40">
              <TabsTrigger
                value="login"
                className="rounded-full text-slate-900 dark:text-white data-[state=active]:bg-[#1E3A8A]/90 data-[state=active]:text-white shadow-sm"
              >
                Login
              </TabsTrigger>
              <TabsTrigger
                value="register"
                className="rounded-full text-slate-900 dark:text-white data-[state=active]:bg-[#1E3A8A]/90 data-[state=active]:text-white shadow-sm"
              >
                Register
              </TabsTrigger>
            </TabsList>

            <TabsContent value="login" className="space-y-4">
              {!otpStep && (
              <form onSubmit={handleLogin} className="space-y-4">
                <div className="space-y-2">
                  <Label htmlFor="login-username" className="text-slate-900 dark:text-white font-medium">Username</Label>
                  <Input
                    id="login-username"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    placeholder="Enter your username"
                    required
                    className="bg-gradient-to-r from-white/65 to-slate-50/40 dark:from-slate-900/65 dark:to-slate-900/45 backdrop-blur rounded-xl ring-1 ring-sky-300/60 dark:ring-sky-700/50 border-0 text-slate-900 dark:text-white placeholder:text-slate-700 dark:placeholder:text-slate-200 focus:ring-2 focus:ring-electric/70"
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="login-password" className="text-slate-900 dark:text-white font-medium">Password</Label>
                  <div className="relative">
                    <Input
                      id="login-password"
                      type={showPassword ? 'text' : 'password'}
                      value={password}
                      onChange={(e) => setPassword(e.target.value)}
                      placeholder="Enter your password"
                      required
                      className="bg-gradient-to-r from-white/65 to-slate-50/40 dark:from-slate-900/65 dark:to-slate-900/45 backdrop-blur rounded-xl ring-1 ring-slate-300/60 dark:ring-slate-700/60 border-0 text-slate-900 dark:text-white placeholder:text-slate-700 dark:placeholder:text-slate-200 focus:ring-2 focus:ring-electric/70"
                    />
                  </div>
                </div>
              {error && <p className="text-sm text-red-500">{error}</p>}
              <div className="text-sm text-right -mt-1">
                <button type="button" className="underline underline-offset-2 text-slate-800 hover:text-slate-900 dark:text-slate-200 dark:hover:text-white font-medium" onClick={() => { setForgotOpen(true); setFpStep('request'); setFpMsg(''); }}>Forgot password?</button>
              </div>
              <Button type="submit" className="w-full h-10 bg-[#1E3A8A] text-white font-bold rounded-[6px] shadow-md hover:bg-[#172554] focus:ring-2 focus:ring-blue-300/60 border-0" disabled={isLoading}>
                {isLoading ? 'Logging in...' : 'Login'}
              </Button>
            </form>
              )}

              {otpStep && (
                <div className="space-y-4">
                  <div>
                    <p className="text-sm text-muted-foreground">Enter the 6-digit OTP sent for this session</p>
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="otp">OTP</Label>
                    <Input
                      id="otp"
                      value={otp}
                      onChange={(e) => setOtp(e.target.value.replace(/[^0-9]/g, '').slice(0, 6))}
                      placeholder="Enter OTP"
                      inputMode="numeric"
                      pattern="[0-9]*"
                      className="bg-gradient-to-r from-white/65 to-slate-50/40 dark:from-slate-900/65 dark:to-slate-900/45 backdrop-blur rounded-xl ring-1 ring-sky-300/60 dark:ring-sky-700/50 border-0 text-slate-900 dark:text-white placeholder:text-slate-700 dark:placeholder:text-slate-200 focus:ring-2 focus:ring-electric/70"
                    />
                  </div>
                  {otpError && <p className="text-sm text-red-500">{otpError}</p>}
                  <div className="flex gap-2">
                    <Button
                      className="flex-1 h-10 bg-[#1E3A8A] text-white font-semibold rounded-[6px] shadow hover:bg-[#172554] focus:ring-2 focus:ring-blue-300/60"
                      onClick={async () => {
                        setOtpError('');
                        if (otp.length !== 6) { setOtpError('Please enter the 6-digit OTP'); return; }
                        const result = await verifyOtp(otp);
                        if (result.success) {
                          const destination = (user && user.role === 'ADMIN') ? '/admin' : '/user';
                          navigate(destination);
                        } else {
                          setOtpError(result.message || 'Invalid or expired OTP');
                        }
                      }}
                    >Verify OTP</Button>
                    <Button variant="outline" className="flex-1 h-10 bg-slate-800 text-white font-semibold rounded-[6px] shadow hover:bg-slate-900 focus:ring-2 focus:ring-slate-300/60 dark:bg-slate-700 dark:hover:bg-slate-800" onClick={() => { setOtpStep(false); setOtp(''); }}>Back</Button>
                  </div>
                </div>
              )}
            </TabsContent>

            <TabsContent value="register" className="space-y-4">
              <form onSubmit={handleRegister} className="space-y-4">
                <div className="space-y-2">
                  <Label htmlFor="register-email" className="text-slate-900 dark:text-white font-medium">Email</Label>
                  <Input
                    id="register-email"
                    type="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    placeholder="Enter your email"
                    required
                    className="bg-gradient-to-r from-white/65 to-slate-50/40 dark:from-slate-900/65 dark:to-slate-900/45 backdrop-blur rounded-xl ring-1 ring-slate-300/60 dark:ring-slate-700/60 border-0 text-slate-900 dark:text-white placeholder:text-slate-700 dark:placeholder:text-slate-200 focus:ring-2 focus:ring-electric/70"
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="register-username" className="text-slate-900 dark:text-white font-medium">Username</Label>
                  <Input
                    id="register-username"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    placeholder="Choose a username"
                    required
                    className="bg-gradient-to-r from-white/65 to-slate-50/40 dark:from-slate-900/65 dark:to-slate-900/45 backdrop-blur rounded-xl ring-1 ring-slate-300/60 dark:ring-slate-700/60 border-0 text-slate-900 dark:text-white placeholder:text-slate-700 dark:placeholder:text-slate-200 focus:ring-2 focus:ring-electric/70"
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="register-password" className="text-slate-900 dark:text-white font-medium">Password</Label>
                  <Input
                    id="register-password"
                    type={showPassword ? 'text' : 'password'}
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    placeholder="Choose a password"
                    required
                    className="bg-gradient-to-r from-white/65 to-slate-50/40 dark:from-slate-900/65 dark:to-slate-900/45 backdrop-blur rounded-xl ring-1 ring-slate-300/60 dark:ring-slate-700/60 border-0 text-slate-900 dark:text-white placeholder:text-slate-700 dark:placeholder:text-slate-200 focus:ring-2 focus:ring-electric/70"
                  />
                </div>
                {error && <p className="text-sm text-red-500">{error}</p>}
                <Button type="submit" className="w-full">
                  Register
                </Button>
              </form>
            </TabsContent>
          </Tabs>

          {/* Preview Mode removed */}
        </CardContent>
      </Card>

      {/* Forgot Password Modal */}
      {forgotOpen && (
        <div className="fixed inset-0 z-[100] bg-black/60 flex items-center justify-center p-4" onClick={() => setForgotOpen(false)}>
          <div className="bg-white dark:bg-slate-900 rounded-lg shadow-xl w-full max-w-sm p-5" onClick={(e) => e.stopPropagation()}>
            <h3 className="text-lg font-semibold mb-2">Forgot Password</h3>
            {fpStep === 'request' ? (
              <div className="space-y-3">
                <div>
                  <Label htmlFor="fp-email">Email</Label>
                  <Input id="fp-email" type="email" value={fpEmail} onChange={(e) => setFpEmail(e.target.value)} placeholder="your@srmist.edu.in" />
                </div>
                {fpMsg && <div className="text-xs text-muted-foreground">{fpMsg}</div>}
                <div className="flex gap-2">
                  <Button className="flex-1" onClick={async () => {
                    setFpMsg('');
                    if (!fpEmail) { setFpMsg('Please enter your email'); return; }
                    try {
                      const exists = await authService.checkEmail(fpEmail);
                      if (!exists) { setFpMsg('No account found for this email'); return; }
                      const res = await authService.forgotPassword(fpEmail);
                      if (res.success) {
                        setFpMsg('Reset token generated. Token shown below.');
                        if (res.token) setFpServerToken(res.token);
                        setFpStep('reset');
                      } else {
                        setFpMsg(res.message || 'Failed to request reset');
                      }
                    } catch (e: any) { setFpMsg(typeof e?.message === 'string' ? e.message : 'Failed to request reset'); }
                  }}>Request reset</Button>
                  <Button variant="outline" className="flex-1" onClick={() => setForgotOpen(false)}>Cancel</Button>
                </div>
              </div>
            ) : (
              <div className="space-y-3">
                <div className="text-xs text-muted-foreground">Use the token shown below and type it into the field.</div>
                <div className="px-2 py-2 rounded bg-slate-100 dark:bg-slate-800 text-center font-mono tracking-widest text-sm select-all">
                  {fpServerToken || '------'}
                </div>
                <div>
                  <Label htmlFor="fp-token">Token</Label>
                  <Input id="fp-token" value={fpToken} onChange={(e) => setFpToken(e.target.value)} placeholder="Type the token manually" />
                </div>
                <div>
                  <Label htmlFor="fp-newpwd">New Password</Label>
                  <Input id="fp-newpwd" type="password" value={fpNewPwd} onChange={(e) => setFpNewPwd(e.target.value)} placeholder="Enter new password" />
                </div>
                {fpMsg && <div className="text-xs text-muted-foreground">{fpMsg}</div>}
                <div className="flex gap-2">
                  <Button className="flex-1" onClick={async () => {
                    setFpMsg('');
                    if (!fpToken || !fpNewPwd) { setFpMsg('Token and new password are required'); return; }
                    try {
                      const res = await authService.resetPassword(fpToken, fpNewPwd);
                      if (res.success) {
                        setFpMsg('Password reset successful. You can now login.');
                        setForgotOpen(false);
                      } else {
                        setFpMsg(res.message || 'Failed to reset password');
                      }
                    } catch (_) { setFpMsg('Failed to reset password'); }
                  }}>Reset</Button>
                  <Button variant="outline" className="flex-1" onClick={() => setForgotOpen(false)}>Close</Button>
                </div>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
