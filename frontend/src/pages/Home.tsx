import {useKeycloak} from "@react-keycloak/web";
import {useEffect, useState} from "react";
import {Card, Row, Col, Statistic} from "antd";
import {Pie} from "@ant-design/plots";

const Home = () => {
    const {keycloak} = useKeycloak();
    const [stats, setStats] = useState({
        totalProducts: 0,
        totalRentals: 0,
        rentalStatuses: {}
    });

    const token = keycloak.token;
    const headers = {Authorization: `Bearer ${token}`};

    useEffect(() => {
        const fetchData = async () => {
            try {
                const [productsRes, rentalsRes] = await Promise.all([
                    fetch("http://localhost:8080/stats/products", {headers}),
                    fetch("http://localhost:8080/stats/rentals", {headers})
                ]);

                const productsData = await productsRes.json();
                const rentalsData = await rentalsRes.json();

                console.log(productsData)
                console.log(rentalsRes)

                setStats({
                    totalProducts: productsData.totalProduct,
                    totalRentals: rentalsData.totalRental,
                    rentalStatuses: rentalsData.statuses
                });

            } catch (error) {
                console.error("Error fetching home data:", error);
            }
        };

        fetchData();
    }, []);

    const rentalStatusData = Object.keys(stats.rentalStatuses).map((status) => {
        const key = status as keyof typeof stats.rentalStatuses;
        return {
            type: status,
            value: stats.rentalStatuses[key],
        };
    });


    return (
        <Row gutter={16}>
            <Col span={8}>
                <Card>
                    <Statistic title="Total Products" value={stats.totalProducts}/>
                </Card>
            </Col>
            <Col span={8}>
                <Card>
                    <Statistic title="Total Rentals" value={stats.totalRentals}/>
                </Card>
            </Col>
            <Col span={12} style={{ marginTop: 20 }}>
                <Card title="Rental Status Distribution">
                    <Pie data={rentalStatusData} angleField="value" colorField="type" />
                </Card>
            </Col>
        </Row>
    );
};

export default Home;