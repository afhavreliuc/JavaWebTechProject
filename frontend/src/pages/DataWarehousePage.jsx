import { useState, useEffect } from 'react';
import { propagationService } from '../services/api';
import { Database, RefreshCw, CheckCircle, AlertTriangle, BarChart3, TrendingUp, Users } from 'lucide-react';
import { 
  LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer,
  BarChart, Bar, Cell
} from 'recharts';

export default function DataWarehousePage() {
  const [stats, setStats] = useState(null);
  const [financialData, setFinancialData] = useState([]);
  const [topDoctorsData, setTopDoctorsData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [syncing, setSyncing] = useState(false);
  const [message, setMessage] = useState(null);

  const fetchData = async () => {
    setLoading(true);
    try {
      const [statsRes, financialRes, doctorsRes] = await Promise.all([
        propagationService.getStats(),
        propagationService.getFinancialEvolution(),
        propagationService.getTopDoctors()
      ]);
      setStats(statsRes.data);
      setFinancialData(financialRes.data);
      setTopDoctorsData(doctorsRes.data);
    } catch (error) {
      console.error('Error fetching DW data:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleSync = async () => {
    setSyncing(true);
    setMessage(null);
    try {
      const res = await propagationService.sync();
      setMessage({ type: 'success', text: res.data });
      fetchData();
    } catch (error) {
      setMessage({ type: 'error', text: 'Eroare la sincronizarea datelor.' });
    } finally {
      setSyncing(false);
    }
  };

  const monthNames = ["Ian", "Feb", "Mar", "Apr", "Mai", "Iun", "Iul", "Aug", "Sep", "Oct", "Noi", "Dec"];
  
  const formattedFinancialData = financialData.map(item => {
    const m = item.MONTH_NUM || item.month_num;
    const cy = item.CURRENT_YEAR || item.current_year || 0;
    const py = item.PREVIOUS_YEAR || item.previous_year || 0;
    
    return {
      name: monthNames[m - 1] || `Luna ${m}`,
      current: cy,
      previous: py
    };
  });

  const formattedTopDoctors = topDoctorsData.map(item => ({
    name: item.DOCTOR_NAME || item.doctor_name,
    revenue: item.TOTAL_REVENUE || item.total_revenue || 0,
    count: item.APPOINTMENT_COUNT || item.appointment_count || 0
  }));

  const isDataValid = (table) => {
    if (!stats) return false;
    return stats[`oltp_${table}`] === stats[`dw_${table}`];
  };

  return (
    <div className="w-full space-y-8">
      <div className="bg-white p-8 rounded-2xl shadow-lg border border-gray-100">
        <div className="flex justify-between items-center mb-8">
          <h2 className="text-3xl font-bold text-gray-800 flex items-center gap-3">
            <Database className="text-indigo-600" size={32} />
            Data Warehouse (DW)
          </h2>
          <button 
            onClick={handleSync}
            disabled={syncing}
            className={`flex items-center gap-2 px-6 py-2 rounded-full font-bold text-white transition-all shadow-md ${
              syncing ? 'bg-gray-400 cursor-not-allowed' : 'bg-indigo-600 hover:bg-indigo-700 active:scale-95'
            }`}
          >
            <RefreshCw size={20} className={syncing ? 'animate-spin' : ''} />
            {syncing ? 'Se sincronizează...' : 'Sincronizează OLTP -> DW'}
          </button>
        </div>

        {message && (
          <div className={`mb-6 p-4 rounded-xl flex items-center gap-3 ${
            message.type === 'success' ? 'bg-green-50 text-green-700' : 'bg-red-50 text-red-700'
          }`}>
            {message.type === 'success' ? <CheckCircle /> : <AlertTriangle />}
            {message.text}
          </div>
        )}

        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          {['patients', 'doctors', 'appointments'].map((table) => (
            <div key={table} className="bg-gray-50 p-6 rounded-2xl border border-gray-100 flex flex-col items-center">
              <h3 className="text-lg font-bold text-gray-700 capitalize mb-4">{table}</h3>
              <div className="flex justify-between w-full mb-2">
                <span className="text-sm text-gray-500">OLTP (Sursă):</span>
                <span className="font-bold text-indigo-600">{stats ? stats[`oltp_${table}`] : '-'}</span>
              </div>
              <div className="flex justify-between w-full mb-4">
                <span className="text-sm text-gray-500">DW (Destinație):</span>
                <span className="font-bold text-indigo-600">{stats ? stats[`dw_${table}`] : '-'}</span>
              </div>
              
              <div className={`w-full py-2 px-4 rounded-lg flex items-center justify-center gap-2 text-xs font-bold ${
                isDataValid(table) ? 'bg-green-100 text-green-700' : 'bg-amber-100 text-amber-700'
              }`}>
                {isDataValid(table) ? (
                  <><CheckCircle size={14} /> Date Validate</>
                ) : (
                  <><AlertTriangle size={14} /> Sincronizare Necesară</>
                )}
              </div>
            </div>
          ))}
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        <div className="bg-white p-6 rounded-2xl shadow-lg border border-gray-100">
          <div className="flex items-center gap-2 mb-6">
            <TrendingUp className="text-indigo-600" size={24} />
            <h3 className="text-xl font-bold text-gray-800">Evoluție Financiară (Trend Analysis)</h3>
          </div>
          <div className="h-[300px] w-full">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={formattedFinancialData}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f0f0f0" />
                <XAxis dataKey="name" axisLine={false} tickLine={false} tick={{fill: '#9ca3af', fontSize: 12}} />
                <YAxis axisLine={false} tickLine={false} tick={{fill: '#9ca3af', fontSize: 12}} />
                <Tooltip 
                  contentStyle={{borderRadius: '12px', border: 'none', boxShadow: '0 10px 15px -3px rgba(0,0,0,0.1)'}}
                />
                <Legend iconType="circle" />
                <Line 
                  type="monotone" 
                  dataKey="current" 
                  name="An Curent" 
                  stroke="#4f46e5" 
                  strokeWidth={3} 
                  dot={{ r: 4, fill: '#4f46e5' }}
                  activeDot={{ r: 6 }}
                />
                <Line 
                  type="monotone" 
                  dataKey="previous" 
                  name="An Anterior" 
                  stroke="#94a3b8" 
                  strokeWidth={2} 
                  strokeDasharray="5 5"
                  dot={{ r: 3, fill: '#94a3b8' }}
                />
              </LineChart>
            </ResponsiveContainer>
          </div>
          <p className="mt-4 text-xs text-gray-500 italic">
            * Comparație lunară a veniturilor totale între anul curent și cel precedent.
          </p>
        </div>

        <div className="bg-white p-6 rounded-2xl shadow-lg border border-gray-100">
          <div className="flex items-center gap-2 mb-6">
            <Users className="text-indigo-600" size={24} />
            <h3 className="text-xl font-bold text-gray-800">Top 5 Medici (Trimestrul Curent)</h3>
          </div>
          <div className="h-[300px] w-full">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={formattedTopDoctors} layout="vertical" margin={{ left: 40, right: 40 }}>
                <CartesianGrid strokeDasharray="3 3" horizontal={false} stroke="#f0f0f0" />
                <XAxis type="number" hide />
                <YAxis 
                  dataKey="name" 
                  type="category" 
                  axisLine={false} 
                  tickLine={false} 
                  tick={{fill: '#4b5563', fontSize: 12, fontWeight: 600}}
                  width={100}
                />
                <Tooltip 
                  cursor={{fill: '#f8fafc'}}
                  contentStyle={{borderRadius: '12px', border: 'none', boxShadow: '0 10px 15px -3px rgba(0,0,0,0.1)'}}
                />
                <Bar dataKey="revenue" name="Venit Total" fill="#4f46e5" radius={[0, 4, 4, 0]} barSize={20}>
                  {formattedTopDoctors.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={index === 0 ? '#4338ca' : '#6366f1'} />
                  ))}
                </Bar>
              </BarChart>
            </ResponsiveContainer>
          </div>
          <div className="mt-4 grid grid-cols-5 gap-2">
            {formattedTopDoctors.map((doc, idx) => (
              <div key={idx} className="text-center">
                <div className="text-[10px] font-bold text-gray-400 truncate">{doc.name}</div>
                <div className="text-xs font-bold text-indigo-600">{doc.count} prog.</div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
