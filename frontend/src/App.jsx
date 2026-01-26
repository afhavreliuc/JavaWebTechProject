import { BrowserRouter as Router, Routes, Route, Link, Navigate, useLocation } from 'react-router-dom';
import { LayoutDashboard, Users, UserCog, CalendarDays, Database, ShieldCheck, LogOut } from 'lucide-react';
import PatientsPage from './pages/PatientsPage';
import DoctorsPage from './pages/DoctorsPage';
import AppointmentsPage from './pages/AppointmentsPage';
import InsuranceProvidersPage from './pages/InsuranceProvidersPage';
import DataWarehousePage from './pages/DataWarehousePage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import { AuthProvider, useAuth } from './context/AuthContext';

function ProtectedRoute({ children, roles }) {
  const { user } = useAuth();
  const location = useLocation();

  if (!user) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  if (roles && !roles.includes(user.role)) {
    return <Navigate to="/" replace />;
  }

  return children;
}

function AppContent() {
  const { user, logout } = useAuth();

  return (
      <div className="min-h-screen bg-gray-100 flex justify-center p-0 md:p-6">
        <div className="w-full max-w-5xl bg-white shadow-2xl flex flex-col min-h-screen md:min-h-0 md:rounded-2xl overflow-hidden">
          <header className="bg-indigo-700 text-white shadow-lg sticky top-0 z-50">
            <div className="px-4 sm:px-6 lg:px-8">
              <div className="flex justify-between items-center h-16">
                <div className="flex items-center gap-8">
                  <Link to="/" className="text-2xl font-bold flex items-center gap-2">
                    <LayoutDashboard size={28} />
                    MedManager
                  </Link>
                  
                {user && (
                  <nav className="hidden md:flex items-center gap-2">
                    {user.role === 'PATIENT' && (
                    <Link to="/patients" className="flex items-center gap-2 py-2 px-4 rounded-lg hover:bg-indigo-600 transition-colors">
                      <Users size={18} />
                      Profilul Meu
                    </Link>
                    )}
                    {user.role === 'DOCTOR' && (
                    <Link to="/patients" className="flex items-center gap-2 py-2 px-4 rounded-lg hover:bg-indigo-600 transition-colors">
                      <Users size={18} />
                      Pacienți
                    </Link>
                    )}
                    <Link to="/doctors" className="flex items-center gap-2 py-2 px-4 rounded-lg hover:bg-indigo-600 transition-colors">
                      <UserCog size={18} />
                      Doctori
                    </Link>
                    <Link to="/appointments" className="flex items-center gap-2 py-2 px-4 rounded-lg hover:bg-indigo-600 transition-colors">
                      <CalendarDays size={18} />
                      Programări
                    </Link>
                    <Link to="/insurance" className="flex items-center gap-2 py-2 px-4 rounded-lg hover:bg-indigo-600 transition-colors">
                      <ShieldCheck size={18} />
                      Asigurări
                    </Link>
                  </nav>
                )}
                </div>

              <div className="flex items-center gap-4">
                {user && (
                  <>
                    <Link to="/dw" className="flex items-center gap-2 py-2 px-4 rounded-lg bg-indigo-800 hover:bg-indigo-900 transition-colors shadow-inner text-sm">
                      <Database size={16} />
                      <span className="hidden sm:inline">DW</span>
                    </Link>
                    <div className="flex items-center gap-3 ml-2 pl-4 border-l border-indigo-500">
                      <span className="text-sm font-medium hidden sm:inline">{user.username} ({user.role})</span>
                      <button 
                        onClick={logout}
                        className="p-2 rounded-full hover:bg-indigo-600 transition-colors"
                        title="Logout"
                      >
                        <LogOut size={20} />
                      </button>
                    </div>
                  </>
                )}
                {!user && (
                  <div className="flex gap-2">
                    <Link to="/login" className="bg-white text-indigo-700 px-4 py-2 rounded-full font-bold hover:bg-gray-100 transition-colors text-sm">
                      Autentificare
                    </Link>
                    <Link to="/register" className="bg-indigo-600 text-white border border-white px-4 py-2 rounded-full font-bold hover:bg-indigo-500 transition-colors text-sm">
                      Înregistrare
                  </Link>
                  </div>
                )}
                </div>
              </div>
            </div>
          </header>

          <main className="flex-1 p-4 md:p-8 overflow-y-auto">
            <Routes>
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route path="/patients" element={
              <ProtectedRoute roles={['PATIENT', 'DOCTOR']}>
                <PatientsPage />
              </ProtectedRoute>
            } />
            <Route path="/doctors" element={
              <ProtectedRoute>
                <DoctorsPage />
              </ProtectedRoute>
            } />
            <Route path="/appointments" element={
              <ProtectedRoute>
                <AppointmentsPage />
              </ProtectedRoute>
            } />
            <Route path="/insurance" element={
              <ProtectedRoute>
                <InsuranceProvidersPage />
              </ProtectedRoute>
            } />
            <Route path="/dw" element={
              <ProtectedRoute roles={['DOCTOR']}>
                <DataWarehousePage />
              </ProtectedRoute>
            } />
              <Route path="/" element={
              user ? (
                <div className="text-center mt-20">
                  <h2 className="text-4xl font-bold text-gray-800">Bine ai venit la MedManager, {user.username}</h2>
                  <p className="text-gray-600 mt-4 max-w-2xl mx-auto text-lg">
                    Sistem integrat pentru gestionarea clinicilor medicale. 
                  </p>
                  <div className="mt-10 grid grid-cols-1 sm:grid-cols-3 gap-6 max-w-4xl mx-auto">
                    {user.role === 'PATIENT' && (
                    <Link to="/patients" className="p-6 bg-white rounded-2xl shadow-md hover:shadow-xl transition-shadow border-b-4 border-indigo-500">
                      <Users className="mx-auto text-indigo-600 mb-4" size={40} />
                      <h3 className="font-bold text-lg text-gray-800">Profilul Meu</h3>
                      <p className="text-sm text-gray-500 mt-2">Vizualizează și editează detaliile tale personale</p>
                    </Link>
                    )}
                    {user.role === 'DOCTOR' && (
                    <Link to="/patients" className="p-6 bg-white rounded-2xl shadow-md hover:shadow-xl transition-shadow border-b-4 border-indigo-500">
                      <Users className="mx-auto text-indigo-600 mb-4" size={40} />
                      <h3 className="font-bold text-lg text-gray-800">Pacienți</h3>
                      <p className="text-sm text-gray-500 mt-2">Gestiune baze de date pacienți și istoric</p>
                    </Link>
                    )}
                    <Link to="/appointments" className="p-6 bg-white rounded-2xl shadow-md hover:shadow-xl transition-shadow border-b-4 border-indigo-500">
                      <CalendarDays className="mx-auto text-indigo-600 mb-4" size={40} />
                      <h3 className="font-bold text-lg text-gray-800">Programări</h3>
                      <p className="text-sm text-gray-500 mt-2">Programări consultații și verificări sloturi</p>
                    </Link>
                    {user.role === 'DOCTOR' && (
                    <Link to="/dw" className="p-6 bg-white rounded-2xl shadow-md hover:shadow-xl transition-shadow border-b-4 border-indigo-500">
                      <Database className="mx-auto text-indigo-600 mb-4" size={40} />
                      <h3 className="font-bold text-lg text-gray-800">DW Analytics</h3>
                      <p className="text-sm text-gray-500 mt-2">Validare și propagare date OLTP -&gt; DW</p>
                    </Link>
                    )}
                  </div>
                </div>
              ) : (
                <Navigate to="/login" replace />
              )
              } />
            </Routes>
          </main>
        </div>
      </div>
  );
}

function App() {
  return (
    <Router>
      <AuthProvider>
        <AppContent />
      </AuthProvider>
    </Router>
  );
}

export default App;

