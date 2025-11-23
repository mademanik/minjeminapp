import { useKeycloak } from "@react-keycloak/web";
import { Navigate } from "react-router-dom";
import { Spin } from "antd";

const AdminRoute: React.FC<{ children: JSX.Element }> = ({ children }) => {
    const { keycloak, initialized } = useKeycloak();

    if (!initialized) {
        return (
            <div style={{ display: "flex", justifyContent: "center", marginTop: "20%" }}>
                <Spin size="large" tip="Checking access role..." />
            </div>
        );
    }

    if (!keycloak.authenticated) {
        return <Navigate to="/" replace />;
    }

    const isAdmin = keycloak.hasRealmRole("admin-role");

    return isAdmin ? children : <Navigate to="/dashboard/products" replace />;
};

export default AdminRoute;
