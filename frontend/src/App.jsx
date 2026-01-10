import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import { LayoutDashboard, Users, UserCog, CalendarDays, Database } from 'lucide-react';
import PatientsPage from './pages/PatientsPage';
import DoctorsPage from './pages/DoctorsPage';
import AppointmentsPage from './pages/AppointmentsPage';
import DataWarehousePage from './pages/DataWarehousePage';

function App() {
  return (
    <Router>
      <div className="min-h-screen bg-gray-100 flex justify-center p-0 md:p-6">
        <div className="w-full max-w-5xl bg-white shadow-2xl flex flex-col min-h-screen md:min-h-0 md:rounded-2xl overflow-hidden">
          {/* Header Navigation */}
          <header className="bg-indigo-700 text-white shadow-lg sticky top-0 z-50">
            <div className="px-4 sm:px-6 lg:px-8">
              <div className="flex justify-between items-center h-16">
                <div className="flex items-center gap-8">
                  <Link to="/" className="text-2xl font-bold flex items-center gap-2">
                    <LayoutDashboard size={28} />
                    MedManager
                  </Link>
                  
                  <nav className="hidden md:flex items-center gap-2">
                    <Link to="/patients" className="flex items-center gap-2 py-2 px-4 rounded-lg hover:bg-indigo-600 transition-colors">
                      <Users size={18} />
                      Pacienți
                    </Link>
                    <Link to="/doctors" className="flex items-center gap-2 py-2 px-4 rounded-lg hover:bg-indigo-600 transition-colors">
                      <UserCog size={18} />
                      Doctori
                    </Link>
                    <Link to="/appointments" className="flex items-center gap-2 py-2 px-4 rounded-lg hover:bg-indigo-600 transition-colors">
                      <CalendarDays size={18} />
                      Programări
                    </Link>
                  </nav>
                </div>

                <div className="flex items-center">
                  <Link to="/dw" className="flex items-center gap-2 py-2 px-4 rounded-lg bg-indigo-800 hover:bg-indigo-900 transition-colors shadow-inner">
                    <Database size={18} />
                    <span className="hidden sm:inline">Data Warehouse</span>
                  </Link>
                </div>
              </div>
            </div>
          </header>

          {/* Main Content */}
          <main className="flex-1 p-4 md:p-8 overflow-y-auto">
            <Routes>
              <Route path="/patients" element={<PatientsPage />} />
              <Route path="/doctors" element={<DoctorsPage />} />
              <Route path="/appointments" element={<AppointmentsPage />} />
              <Route path="/dw" element={<DataWarehousePage />} />
              <Route path="/" element={
                <div className="text-center mt-20">
                  <h2 className="text-4xl font-bold text-gray-800">Bine ai venit la MedManager</h2>
                  <p className="text-gray-600 mt-4 max-w-2xl mx-auto text-lg">
                    Sistem integrat pentru gestionarea clinicilor medicale. 
                    Administrează pacienții, programează consultații și analizează datele în timp real.
                  </p>
                  <div className="mt-10 grid grid-cols-1 sm:grid-cols-3 gap-6 max-w-4xl mx-auto">
                    <Link to="/patients" className="p-6 bg-white rounded-2xl shadow-md hover:shadow-xl transition-shadow border-b-4 border-indigo-500">
                      <Users className="mx-auto text-indigo-600 mb-4" size={40} />
                      <h3 className="font-bold text-lg text-gray-800">Pacienți</h3>
                      <p className="text-sm text-gray-500 mt-2">Gestiune baze de date pacienți și istoric</p>
                    </Link>
                    <Link to="/appointments" className="p-6 bg-white rounded-2xl shadow-md hover:shadow-xl transition-shadow border-b-4 border-indigo-500">
                      <CalendarDays className="mx-auto text-indigo-600 mb-4" size={40} />
                      <h3 className="font-bold text-lg text-gray-800">Programări</h3>
                      <p className="text-sm text-gray-500 mt-2">Programări consultații și verificări sloturi</p>
                    </Link>
                    <Link to="/dw" className="p-6 bg-white rounded-2xl shadow-md hover:shadow-xl transition-shadow border-b-4 border-indigo-500">
                      <Database className="mx-auto text-indigo-600 mb-4" size={40} />
                      <h3 className="font-bold text-lg text-gray-800">DW Analytics</h3>
                      <p className="text-sm text-gray-500 mt-2">Validare și propagare date OLTP -&gt; DW</p>
                    </Link>
                  </div>
                </div>
              } />
            </Routes>
          </main>
        </div>
      </div>
    </Router>
  );
}

export default App;

