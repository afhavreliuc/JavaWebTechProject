import { useState, useEffect } from 'react';
import { insuranceProviderService } from '../services/api';
import { Plus, Trash2, Edit2, CheckCircle2, AlertCircle, Loader2, ShieldCheck } from 'lucide-react';

export default function InsuranceProvidersPage() {
  const [providers, setProviders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [editId, setEditId] = useState(null);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState(null);
  const [formData, setFormData] = useState({
    name: '',
    contactNumber: ''
  });

  const fetchProviders = async () => {
    try {
      const response = await insuranceProviderService.getAll();
      setProviders(response.data);
      setLoading(false);
    } catch (error) {
      console.error('Error fetching insurance providers:', error);
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProviders();
  }, []);

  const handleEdit = (provider) => {
    setEditId(provider.id);
    setFormData({
      name: provider.name,
      contactNumber: provider.contactNumber || ''
    });
    setShowForm(true);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setMessage(null);
    try {
      if (editId) {
        await insuranceProviderService.update(editId, formData);
        setMessage({ type: 'success', text: 'Asigurător actualizat cu succes!' });
      } else {
        await insuranceProviderService.create(formData);
        setMessage({ type: 'success', text: 'Asigurător adăugat cu succes!' });
      }
      
      setShowForm(false);
      setEditId(null);
      setFormData({ name: '', contactNumber: '' });
      fetchProviders();
    } catch (error) {
      console.error('Error saving insurance provider:', error);
      setMessage({ 
        type: 'error', 
        text: error.response?.data?.message || 'Eroare la salvarea asigurătorului.' 
      });
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (id) => {
    if (window.confirm('Ești sigur că vrei să ștergi acest asigurător? Această acțiune poate afecta pacienții asociați.')) {
      try {
        await insuranceProviderService.delete(id);
        fetchProviders();
      } catch (error) {
        console.error('Error deleting insurance provider:', error);
        alert('Nu s-a putut șterge asigurătorul. Este posibil să fie utilizat de pacienți.');
      }
    }
  };

  return (
    <div className="w-full">
      <div className="flex justify-between items-center mb-8">
        <h2 className="text-3xl font-bold text-gray-800">Gestionare Asigurări</h2>
        <button 
          onClick={() => {
            if (showForm && editId) {
              setEditId(null);
              setFormData({ name: '', contactNumber: '' });
            } else {
              setShowForm(!showForm);
            }
          }}
          className="bg-indigo-600 text-white px-4 py-2 rounded-lg flex items-center gap-2 hover:bg-indigo-700"
        >
          <Plus size={20} />
          {editId ? 'Mod Nou Asigurător' : 'Adaugă Asigurător'}
        </button>
      </div>

      {message && (
        <div className={`mb-6 p-4 rounded-lg flex items-center gap-3 animate-in fade-in duration-300 ${
          message.type === 'success' ? 'bg-green-50 text-green-700 border border-green-200' : 'bg-red-50 text-red-700 border border-red-200'
        }`}>
          {message.type === 'success' ? <CheckCircle2 size={20} /> : <AlertCircle size={20} />}
          <p className="font-medium">{message.text}</p>
        </div>
      )}

      {showForm && (
        <form onSubmit={handleSubmit} className="bg-white p-6 rounded-xl shadow-md mb-8 grid grid-cols-2 gap-4 border border-indigo-100 ring-2 ring-indigo-500/10">
          <h3 className="col-span-2 text-lg font-bold text-indigo-900 mb-2 border-b pb-2">
            {editId ? `Editare Asigurător #${editId}` : 'Adăugare Asigurător Nou'}
          </h3>
          <div className="flex flex-col gap-1">
            <label className="text-sm font-semibold text-gray-600">
              Nume Asigurător <span className="text-blue-600">*</span>
            </label>
            <input 
              type="text" 
              required
              disabled={saving}
              className="border p-2 rounded-md focus:ring-2 focus:ring-indigo-500 outline-none"
              value={formData.name}
              onChange={(e) => setFormData({...formData, name: e.target.value})}
            />
          </div>
          <div className="flex flex-col gap-1">
            <label className="text-sm font-semibold text-gray-600">
              Număr Contact
            </label>
            <input 
              type="text" 
              disabled={saving}
              className="border p-2 rounded-md focus:ring-2 focus:ring-indigo-500 outline-none"
              value={formData.contactNumber}
              onChange={(e) => setFormData({...formData, contactNumber: e.target.value})}
            />
          </div>
          
          <div className="col-span-2 flex justify-end gap-3 mt-2">
            <button 
              type="button" 
              onClick={() => setShowForm(false)}
              disabled={saving}
              className="text-gray-600 px-4 py-2 hover:bg-gray-100 rounded-lg transition-colors"
            >
              Anulează
            </button>
            <button 
              type="submit" 
              disabled={saving}
              className="bg-indigo-600 text-white px-6 py-2 rounded-lg hover:bg-indigo-700 transition-all flex items-center gap-2 disabled:opacity-70 disabled:cursor-not-allowed shadow-md"
            >
              {saving ? (
                <>
                  <Loader2 className="animate-spin" size={18} />
                  Se salvează...
                </>
              ) : 'Salvează'}
            </button>
          </div>
        </form>
      )}

      {loading ? (
        <div className="text-center py-10">Se încarcă...</div>
      ) : (
        <div className="bg-white rounded-xl shadow-md overflow-hidden">
          <table className="w-full text-left">
            <thead className="bg-gray-50 border-b">
              <tr>
                <th className="px-6 py-4 font-semibold text-gray-700">ID</th>
                <th className="px-6 py-4 font-semibold text-gray-700">Nume</th>
                <th className="px-6 py-4 font-semibold text-gray-700">Contact</th>
                <th className="px-6 py-4 font-semibold text-gray-700">Acțiuni</th>
              </tr>
            </thead>
            <tbody className="divide-y">
              {providers.map((provider) => (
                <tr key={provider.id} className="hover:bg-gray-50 transition-colors">
                  <td className="px-6 py-4 text-gray-600">#{provider.id}</td>
                  <td className="px-6 py-4 font-medium text-gray-900 flex items-center gap-2">
                    <ShieldCheck size={18} className="text-indigo-500" />
                    {provider.name}
                  </td>
                  <td className="px-6 py-4 text-gray-600">{provider.contactNumber || '-'}</td>
                  <td className="px-6 py-4 flex gap-3">
                    <button 
                      onClick={() => handleEdit(provider)}
                      className="text-indigo-600 hover:text-indigo-900"
                    >
                      <Edit2 size={18} />
                    </button>
                    <button 
                      onClick={() => handleDelete(provider.id)}
                      className="text-red-600 hover:text-red-900"
                    >
                      <Trash2 size={18} />
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          {providers.length === 0 && (
            <div className="text-center py-10 text-gray-500">Nu există asigurători înregistrați.</div>
          )}
        </div>
      )}
    </div>
  );
}

